package io.scheduler.comparison.quartz.jobs.pagination

fun interface PageableJobData<K> : Iterable<List<K>>