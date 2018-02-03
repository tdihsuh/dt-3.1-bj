define(['app'],function(app){
	app.controller("clusterDetailCtrl",["$scope", "$http", 'hs_es', '$routeParams', '$timeout', '$rootScope', function($scope, $http, hs_es,$routeParams, $timeout, $rootScope){
		var nodeId = $routeParams.param;
		/** 获取节点详情* */
		$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats").success(function(data){
			$scope.nodeDetail = data.nodes[nodeId];
		});
		/** 获取文档详情* */
		$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats/indices").success(function(data){
			var indices = data.nodes[nodeId].indices;
			indices.store.size_in_bytes =dataformart(indices.store.size_in_bytes);
			indices.merges.current_size_in_bytes = dataformart(indices.merges.current_size_in_bytes);
			$scope.indices = indices;
		});
		/** 获取disk详情* */
		$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats/fs").success(function(data){
			var fs = data.nodes[nodeId].fs;
			fs.total.total_in_bytes = dataformart(fs.total.total_in_bytes);
			fs.total.free_in_bytes = dataformart(fs.total.free_in_bytes);
			fs.total.available_in_bytes = dataformart(fs.total.available_in_bytes);
			//fs.total.disk_reads = dataformart(fs.total.disk_reads);
			//fs.total.disk_writes = dataformart(fs.total.disk_writes);
			fs.total.disk_io_op = dataformart(fs.total.disk_io_op);
			$scope.fs = fs.total;
			
		});
		/** 获取network详情* */
		$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats/transport").success(function(data){
 
			data.nodes[nodeId].transport.rx_size_in_bytes = dataformart(data.nodes[nodeId].transport.rx_size_in_bytes);
			data.nodes[nodeId].transport.tx_size_in_bytes = dataformart(data.nodes[nodeId].transport.tx_size_in_bytes);
			
			
			$scope.network = data.nodes[nodeId].transport;
		});
		/**后退返回主页事件**/
		$scope.back = function (){
			$rootScope.app_nav = "首页 / 集群监控";
			location = "#/cluster/";
		};
		
		function dataformart(input){
			if(input < 1024){
				return input.toFixed(2)+"b";
			}else if(input/1024 >=1 && input/1024 <1024){
				return (input/1024).toFixed(2)+"KB";
			}else if (input/1024/1024 >=1 && input/1024/1024< 1024) {
				return (input/1024/1024).toFixed(2)+"MB";
			}else if (input/1024/1024/1024 >= 1 && input/1024/1024/1024 < 1024) {
				return (input/1024/1024/1024).toFixed(2)+"GB";
			}else if (input/1024/1024/1024/1024 >=1 && input/1024/1024/1024/1024 < 1024) {
				return (input/1024/1024/1024/1024).toFixed(2) +"TB";
			}
		}
		/** cpu* */
		$(function () {
		    $(document).ready(function() {
		    	/** 定时刷新数据* */
		    	var timerId;
		    	var oslist;
				var jvmlist;
				$scope.timeout = function(){
					timerId = $timeout(function(){
						$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats/os").success(function(data){
							oslist = data;
	        			}); 
						$http.get(hs_es.host_name + "/_nodes/"+ nodeId +"/stats/jvm").success(function(data){
							jvmlist = data;
	        			}); 
						$scope.timeout();
					},1000);
				};
				$scope.$on('$destroy', function() {
		            $timeout.cancel(timerId);
		        });
				$scope.timeout();
		       /*
				 * Highcharts.setOptions({ global: { useUTC: false } });
				 */ 
		        var chart1 = $('#img-os-state').highcharts({                                                
		            chart: {                                                                
		                type: 'spline',                                                     
		                animation: Highcharts.svg, // don't animate in old IE
		                marginRight: 10,                                                    
		                events: {                                                           
		                    load: function() {                                              
		                        // set up the updating of the chart each second
		                        var series = this.series[0];                                
		                        setInterval(function() {                                    
		                            var x = (new Date()).getTime(),y = 0;
									if(oslist)
		                                y = oslist.nodes[nodeId].os.cpu.usage;                                  
		                            series.addPoint([x, y], true, true);                    
		                        }, 3000);                                                   
		                    }                                                               
		                }                                                                   
		            },  
		            credits:{  
	                    enabled: false,  
	                    href: "http://www.hansight.com",  
	                    text: '瀚思安信'  
	                }, 
		            title: {                                                                
		                text: 'CPU状态图'                                            
		            },                                                                      
		            xAxis: {                                                                
		                type: 'datetime',                                                   
		                tickPixelInterval: 150                                              
		            },                                                                      
		            yAxis: {                                                                
		                title: {                                                            
		                    text: 'cpu使用率（%）'                                                   
		                },                                                                  
		                plotLines: [{                                                       
		                    value: 0,                                                       
		                    width: 1,                                                       
		                    color: '#808080'                                                
		                }]                                                                  
		            },                                                                      
		            tooltip: {                                                              
		                formatter: function() {                                             
		                        return '<b>'+ this.series.name +'</b><br/>'+                
		                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
		                        Highcharts.numberFormat(this.y, 2);                         
		                }                                                                   
		            },                                                                      
		            legend: {                                                               
		                enabled: false                                                      
		            },                                                                      
		            exporting: {                                                            
		                enabled: false                                                      
		            },                                                                      
		            series: [{                                                              
		                name: 'CPU useage（%）',                                                
		                data: (function() {                                                 
		                    // generate an array of random data
		                    var data = [],                                                  
		                        time = (new Date()).getTime(),                              
		                        i;                                                          
		                                                                                    
		                    for (i = -19; i <= 0; i++) {                                    
		                        data.push({                                                 
		                            x: time + i * 3000,                                     
		                            y: 0                                  
		                        });                                                         
		                    }                                                               
		                    return data;                                                    
		                })()                                                                
		            }]                                                                      
		        });                                                                         
		       
		    
		    /** jvm* */
		    var chart2 = $('#img-jvm-state').highcharts({                                                
	            chart: {                                                                
	                type: 'spline',                                                     
	                animation: Highcharts.svg, // don't animate in old IE
	                marginRight: 10,                                                    
	                events: {                                                           
	                    load: function() {                                              
	                        // set up the updating of the chart each second
	                        var series = this.series[0];                                
	                        setInterval(function() {                                    
	                            var x = (new Date()).getTime(), y=0;
								if(jvmlist)
	                                y = jvmlist.nodes[nodeId].jvm.mem.heap_used_percent;                                  
	                            series.addPoint([x, y], true, true);                    
	                        }, 3000);                                                   
	                    }                                                               
	                }                                                                   
	            },  
	            credits:{  
                    enabled: false,  
                    href: "http://www.hansight.com",  
                    text: '瀚思安信'  
                }, 
	            title: {                                                                
	                text: 'JVM状态图'                                            
	            },                                                                      
	            xAxis: {                                                                
	                type: 'datetime',                                                   
	                tickPixelInterval: 150                                              
	            },                                                                      
	            yAxis: {                                                                
	                title: {                                                            
	                    text: 'JVM使用率（%）'                                                   
	                },                                                                  
	                plotLines: [{                                                       
	                    value: 0,                                                       
	                    width: 1,                                                       
	                    color: '#808080'                                                
	                }]                                                                  
	            },                                                                      
	            tooltip: {                                                              
	                formatter: function() {                                             
	                        return '<b>'+ this.series.name +'</b><br/>'+                
	                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
	                        Highcharts.numberFormat(this.y, 2);                         
	                }                                                                   
	            },                                                                      
	            legend: {                                                               
	                enabled: false                                                      
	            },                                                                      
	            exporting: {                                                            
	                enabled: false                                                      
	            },                                                                      
	            series: [{                                                              
	                name: 'JVM使用率（%）',                                                
	                data: (function() {                                                 
	                    // generate an array of random data
	                    var data = [],                                                  
	                        time = (new Date()).getTime(),                              
	                        i;                                                          
	                                                                                    
	                    for (i = -19; i <= 0; i++) {                                    
	                        data.push({                                                 
	                            x: time + i * 3000,                                     
	                            y: 0                                  
	                        });                                                         
	                    }                                                               
	                    return data;                                                    
	                })()                                                                
	            }]                                                                      
	        });  
		                                                                                    
		}); 
		});
	}])
});