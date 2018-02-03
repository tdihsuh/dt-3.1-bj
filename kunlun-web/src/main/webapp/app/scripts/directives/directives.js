define(['app','../charts/nodeCharts'],function(app,nodeCharts){
	app.directive('onFinishRenderFilters', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                if (scope.$last === true) {
                    $timeout(function () {
                        scope.$emit('ngRepeatFinished');
                    });
                }
            }
        }
    });
	app.directive('showPage',function(){
		return {
			restrict:'A',
			templateUrl:'app/template/common/pagination.html',
			replace:true,
			link:function(scope,element,attrs){
				var pages = [];
				for(var i=0;i<attrs.pageNum;i++){
					pages[i] = i+1;
				}
				scope.pages = pages;
			}
		}
	});
	app.directive('nodeCharts', [function ($timeout){
		'use strict';
		return {
			restrict: 'A',
			replace: true,
			template: '<div class="chart"></div>',
			scope:{
				data: '=data',
			},
			link: function(scope, element, attrs) {
				nodeCharts(element[0],scope.data);
			}
		};
	}]);
});