package com.hansight.kunlun.analysis.realtime.model;

public class AnomalyStatus {
	private Long occur = 0L;

	public AnomalyStatus() {
		super();
	}

	public Long getOccur() {
		return occur;
	}

	public void setOccur(Long occur) {
		this.occur = occur;
	}
	
	public void incOccur(){
		this.occur++;
	}
}
