package com.github.dynamo.backlog;

import java.time.LocalDateTime;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dynamo.core.jackson.LocalDateTimeSerializer;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.core.model.TaskExecutor;

public class TaskSubmission {
	
	private static AtomicLong lastSubmissionId = new AtomicLong( 0 );
	
	private long submissionId;
	private Task task;
	private TaskExecutor<?> executor;
	private Future<?> future;
	private LocalDateTime minDate;

	public TaskSubmission( Task task, TaskExecutor<?> executor, LocalDateTime minDate ) {
		super();
		this.task = task;
		this.executor = executor;
		this.minDate = minDate;
		
		submissionId = lastSubmissionId.getAndIncrement();
	}
	
	public long getSubmissionId() {
		return submissionId;
	}
	
	public Task getTask() {
		return task;
	}
	
	public TaskExecutor<?> getExecutor() {
		return executor;
	}
	
	public Future<?> getFuture() {
		return future;
	}
	
	@JsonSerialize(using=LocalDateTimeSerializer.class)
	public LocalDateTime getMinDate() {
		return minDate;
	}
	
	public void setMinDate(LocalDateTime minDate) {
		this.minDate = minDate;
	}

	public void setFuture(Future<?> future) {
		this.future = future;
	}

	@Override
	public boolean equals(Object obj) {
		return ((TaskSubmission)obj).getSubmissionId() == submissionId;
	}

}
