var hsTimes = {
	/**前几天**/
	getBeforeDateTimeByDay : function(num,datetime){
		if(datetime){
			datetime = new Date(datetime);
		}else{
			datetime = new Date();
		}
		datetime.setDate(datetime.getDate()-num);
		return this.formatTime(datetime)
	},
	/**前几小时**/
	getBeforeDateTimeByHour : function(num,datetime){
		return this.getBeforeDateTime(num*60*60*1000,datetime);
	},
	/***前几分**/
	getBeforeDateTimeByMinute : function(num,datetime){
		return this.getBeforeDateTime(num*60*1000,datetime);
	},
	/***前几秒**/
	getBeforeDateTimeBySecond : function(num,datetime){
		return this.getBeforeDateTime(num*1000,datetime);
	},
	/***前几个月**/
	getBeforeDateTimeByMonth : function(num,datetime){
		if(datetime){
			datetime = new Date(datetime);
		}else{
			datetime = new Date();
		}
		datetime.setMonth(datetime.getMonth()-num);
		return this.formatTime(datetime);
	},
	/**取某月的第一天**/
	getFirstDayOnMonth : function(date){
		if(!date)
			date = new Date();
		var d = new Date();
		this.copyDateTime(d,date);
		d.setDate(1);
		return this.formatTime(d);
	},
	/**取某月的最后一天**/
	getLastDayOnMonth : function(date){
		if(!date)
			date = new Date();
		var d = new Date();
		this.copyDateTime(d,date);
		d.setMonth(date.getMonth()+1);
		d.setDate(1);
		return this.getBeforeDateTime(24*60*60*1000,this.formatLastTime(d));
	},
	/**返回某年的第一天**/
	getFirstDayOnYear : function(date){
		if(!date)
			date = new date();
		var d = new Date();
		this.copyDateTime(d,date);
		d.setMonth(0);
		d.setDate(1);
		return this.formatTime(d);
	},
	/**返回某年的最后一天**/
	getLastDayOnYear : function(date){
		if(!date)
			date = new date();
		var d = new Date();
		this.copyDateTime(d,date);
		d.setMonth(11);
		d.setDate(31);
		return this.formatLastTime(d);
	},
	getFistDayOnPreWeek : function(){
		var date = new Date();
		date.setDate(date.getDate()-date.getDay()-7);
		return this.formatTime(date);
	},
	getLastDayOnPreWeek : function(){
		var date = new Date();
		date.setDate(date.getDate()-date.getDay()-1);
		return this.formatLastTime(date);
	},
	/**
	  *两个日期相隔天数
	  *
	**/
	getDayBetweenDate : function(date1,date2){
		return parseInt(Math.abs(date1.getTime()- date2.getTime())/1000/60/60/24);
	},
	/**
	 * 获取几天之前的日期
	 * num : 时间差值
	 * datetime : 日期（默认系统当前日期）
	**/
	getBeforeDateTime : function(num,datetime){
		if(datetime){
			datetime = new Date(datetime);
		}else{
			datetime = new Date();
		}
		return new Date(datetime.getTime() - num);
	},
	/**
	  *获取两个日期之间的日期
	  **/
	getDaysBetweenDate : function(start,end){
		var num = this.getDayBetweenDate(start,end);
		var list = [];
		for(var i = num;i >= 0;i--){
			list.push(this.getBeforeDateTimeByDay(i,end));
		}
		return list;
	},
	/***格式化时分秒**/
	formatTime : function(date){
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setMilliseconds(0);
		
		return date;
	},
	/***格式化时分秒**/
	formatLastTime : function(date){
		date.setHours(23);
		date.setMinutes(59);
		date.setSeconds(59);
		date.setMilliseconds(0);
		
		return date;
	},
	copyDateTime : function(datetime1,datetime2){
		datetime1.setFullYear(datetime2.getFullYear());
		datetime1.setMonth(datetime2.getMonth());
		datetime1.setDate(datetime2.getDate());
		datetime1.setHours(datetime2.getHours());
		datetime1.setMinutes(datetime2.getMinutes());
		datetime1.setSeconds(datetime2.getSeconds());
		datetime1.setMilliseconds(datetime2.getMilliseconds());
	},
	/**获取指定的时间**/
	getBeforeDateBySpecify : function(timeUnit,timeValue,now){
		if(now == null) now = new Date();
		var date;
		switch(timeUnit){
			case 'second':
				date = this.getBeforeDateTimeBySecond(timeValue,now);
				break;
			case 'minute':
				date = this.getBeforeDateTimeByMinute(timeValue,now);
				break;
			case 'hour':
				date = this.getBeforeDateTimeByHour(timeValue,now);
				break;
			case 'day':
				date = this.getBeforeDateTimeByDay(timeValue,now);
				break;
			case 'week':
				date = this.getBeforeDateTimeByDay(timeValue*7,now);
				break;
			case 'month':
				date = this.getBeforeDateTimeByMonth(timeValue,now);
				break;
			case 'quarter':
				date = this.getBeforeDateTimeByMonth(timeValue*3,now);
				break;
			case 'year':
				date = this.getBeforeDateTimeByYear(timeValue,now);
				break;
			default:
				date = this.getBeforeDateTimeByMinute(5,now);
		}
		return date;
	}
}