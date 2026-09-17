package com.github.novicezk.midjourney.controller;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.dto.TaskConditionDTO;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Api(tags = "任务查询")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
	private final TaskStoreService taskStoreService;
	private final DiscordLoadBalancer discordLoadBalancer;
	private final RestTemplate restTemplate;

	@ApiOperation(value = "指定ID获取任务")
	@GetMapping("/{id}/fetch")
	public Task fetch(@ApiParam(value = "任务ID") @PathVariable String id) {
		return findTask(id);
	}

	@ApiOperation(value = "下载任务图片")
	@GetMapping("/{id}/download")
	public ResponseEntity<byte[]> download(@ApiParam(value = "任务ID") @PathVariable String id) {
		Task task = findTask(id);
		if (task == null || CharSequenceUtil.isBlank(task.getImageUrl())) {
			return ResponseEntity.notFound().build();
		}
		try {
			URI imageUri = new URI(task.getImageUrl());
			if (!"https".equalsIgnoreCase(imageUri.getScheme())) {
				return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
			}
			ResponseEntity<byte[]> imageResponse = this.restTemplate.getForEntity(imageUri, byte[].class);
			if (!imageResponse.getStatusCode().is2xxSuccessful() || imageResponse.getBody() == null) {
				return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
			}
			HttpHeaders headers = new HttpHeaders();
			MediaType contentType = imageResponse.getHeaders().getContentType();
			headers.setContentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType);
			String extension = contentType != null && "webp".equals(contentType.getSubtype()) ? ".webp"
					: contentType != null && "jpeg".equals(contentType.getSubtype()) ? ".jpg" : ".png";
			headers.setContentDisposition(ContentDisposition.attachment().filename("midjourney-" + id + extension).build());
			return new ResponseEntity<>(imageResponse.getBody(), headers, HttpStatus.OK);
		} catch (URISyntaxException | RestClientException exception) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
	}

	private Task findTask(String id) {
		Optional<Task> queueTaskOptional = this.discordLoadBalancer.getQueueTasks().stream()
				.filter(t -> CharSequenceUtil.equals(t.getId(), id)).findFirst();
		return queueTaskOptional.orElseGet(() -> this.taskStoreService.get(id));
	}

	@ApiOperation(value = "查询任务队列")
	@GetMapping("/queue")
	public List<Task> queue() {
		return this.discordLoadBalancer.getQueueTasks().stream()
				.sorted(Comparator.comparing(Task::getSubmitTime))
				.toList();
	}

	@ApiOperation(value = "查询所有任务")
	@GetMapping("/list")
	public List<Task> list() {
		return this.taskStoreService.list().stream()
				.sorted((t1, t2) -> CompareUtil.compare(t2.getSubmitTime(), t1.getSubmitTime()))
				.toList();
	}

	@ApiOperation(value = "根据ID列表查询任务")
	@PostMapping("/list-by-condition")
	public List<Task> listByIds(@RequestBody TaskConditionDTO conditionDTO) {
		if (conditionDTO.getIds() == null) {
			return Collections.emptyList();
		}
		List<Task> result = new ArrayList<>();
		Set<String> notInQueueIds = new HashSet<>(conditionDTO.getIds());
		this.discordLoadBalancer.getQueueTasks().forEach(t -> {
			if (conditionDTO.getIds().contains(t.getId())) {
				result.add(t);
				notInQueueIds.remove(t.getId());
			}
		});
		notInQueueIds.forEach(id -> {
			Task task = this.taskStoreService.get(id);
			if (task != null) {
				result.add(task);
			}
		});
		return result;
	}

}
