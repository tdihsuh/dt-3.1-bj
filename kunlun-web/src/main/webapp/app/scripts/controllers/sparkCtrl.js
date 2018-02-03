define([ 'app' ], function(app) {
	app.controller("sparkCtrl", [
			"$scope",
			"$http",
			function($scope, $http) {
				$("#cmbbody").css("overflow","auto");
				$scope.options = {
						animate:false,
						barColor : '#A57225',
						trackColor : '#EEEEEE',
						scaleColor : false,
						lineWidth : 5,
						lineCap : 'circle',
						size : 50
				};
				
				function blockGet($http, $scope, hosts){
					return function() {
						for (var i=0; i<hosts.length; i++) {
							$http.get("monitor/spark/worker.hs?host=" + hosts[i]).success(function(data, status, headers, config) {
								console.log($scope.master.nodes);
								for (var i=0; i<$scope.master.nodes.length; i++) {
									if (name = $scope.master.nodes[i].name) {
										var node = $scope.master.nodes[i];
										console.log(node);
										node.cpu = myper(data, "worker.coresFree", "worker.coresUsed");
										node.mem = myper(data, "worker.memFree_MB", "worker.memUsed_MB");
										nodeload = Math.floor((node.cpu + node.mem) / 2);
									}
								}
							});
						}
					};
				};
				var getHosts = function($http, $scope) {
					$http.get("monitor/spark/workers.hs").success(function(data, status, headers, config) {
						var hosts = data["hosts"];
						console.log(hosts);
						var nodes = [];
						for (var i=0; i<hosts.length; i++) {
							nodes.push({name:hosts[i], cpu:0, mem:0, load:0});
						}
						$scope.master = {};
						$scope.master.nodes = nodes;
						console.log(hosts[i]);
						var ajaxGet = blockGet($http, $scope, hosts);
						ajaxGet();
						setInterval(ajaxGet, 5000);
						
					});
				};
				getHosts($http, $scope);
			} ]);
	function myper(data, free, used) {
		return Math.floor((data.gauges[used].value
				/ (data.gauges[used].value + data.gauges[free].value)) * 100);
	}
});
