function HsCharts(){
	this.data=null;
	this.objId=null;
	this.tooltip_bgColor=null;
	this.tooltip_color=null;
	this.series_color=null;
	this.column_border_color = null;
	this.xAxis_categories = null;
	this.xAxis_rotation = null;
	this.title = null;
	this.charts={
		series: [{
			showInLegend:false
		}],
		chart: {
			type: 'column',
			margin:0,
			backgroundColor: 'rgba(0,0,0,0)'
		},
		colors:[
			'#f8d7af',
			'#a89f92',
			'#b1a4a0',
			'#57889C',
			'#71843F',
			'#484B4E',
			'#CB4B4B',
			'#9440ED',
			'#57889C'
			/*'#AFD8F8',
			'#92A2A8',
			'#A3B1A0',
			'#57889C',
			'#71843F',
			'#484B4E',
			'#CB4B4B',
			'#9440ED',
			'#57889C'*/
		],
		title: {
			text: ''
		},
		subtitle: {
			text: ''
		},
		yAxis: {
			allowDecimals: false,
			title: {
				text: ''
			},
			gridLineWidth:0,
			lineWidth:0,
			labels: {
				enabled: false
			}
		},
		xAxis : {
			gridLineWidth:0,
			lineWidth:0,
			labels: {
				enabled: false
			},
			showFirstLabel:false,
			tickWidth:0
		},
		tooltip: {
			borderWidth:0,
			pointFormat: '{point.y}'
		},
		plotOptions: {
			column:{
				shadow:false
			}
		},
		legend: {
			enabled: false
		},
		exporting: { 
			enabled: false
		},
		credits: {  
			enabled: false
		}
	};
	
	this.column = function(){
		this.charts["series"][0]["data"] = this.data;
		if(this.tooltip_bgColor)
			//this.charts["tooltip"]["backgroundColor"] = this.tooltip_bgColor;
		if(this.tooltip_color){
			var colors = "color:"+this.tooltip_color;
			this.charts["tooltip"]["formatter"] = function(){return Highcharts.numberFormat(this.point.y,0)};
		}
		if(this.series_color)
			this.charts["series"][0]["color"] = this.series_color;
		if(this.column_border_color)
			this.charts["plotOptions"]["column"]["borderColor"] = this.column_border_color;
		if(this.title)
			this.charts["title"]["text"] = this.title;
		$("#"+this.objId).highcharts(this.charts);
	};
	
	this.stackedColumn = function(){
		this.charts["series"] = this.data;
		this.charts["plotOptions"]["column"]["stacking"] = "normal";
		this.charts["xAxis"] = {
			categories : this.xAxis_categories
		};
		this.charts["yAxis"] = {
			title: {
				text: ''
			},
			gridLineWidth:0,
			lineWidth:0,
			stackLabels:{
				enabled: true,
				style: {
					fontWeight: 'bold',
					color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
				}
			}
		};
		this.charts["tooltip"] = {
			formatter: function() {
				return '<b>'+ this.x +'</b><br/>'+
					'<a href="http://localhost:8080/#/dashboardList" style="color:#0000FF">'+this.series.name +': '+ this.y+'</a>';
			}
		};
		if(this.xAxis_rotation){
			this.charts["xAxis"]["labels"]={};
			this.charts["xAxis"]["labels"]["rotation"] = this.xAxis_rotation;
		}
		if(this.title)
			this.charts["title"]["text"] = this.title;
		delete this.charts["chart"]["margin"];
		$("#"+this.objId).highcharts(this.charts);
	}
	
	this.pie = function(){
		this.charts["series"][0]["type"] = "pie";
		this.charts["series"][0]["data"] = this.data;
		this.charts["series"][0]["name"] = "所占比例";
		this.charts["tooltip"] = {pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'};
		this.charts["plotOptions"] = {
			pie: {
				allowPointSelect: true,
				cursor: 'pointer',
				dataLabels: {
					enabled: true,
					distance: -50,
					format: '<b>{point.name}</b>: {point.percentage:.1f} %',
					color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'white'
				}
			}
		}
		if(this.title)
			this.charts["title"]["text"] = this.title;
		$("#"+this.objId).highcharts(this.charts);
	}
}
