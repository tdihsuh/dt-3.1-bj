define(['app'],function(app){
	app.config(['$routeProvider',function ($routeProvider) {
		$routeProvider.when('/',{
		templateUrl:'app/template/dashboard/dashboard.html',
			controller:'dashboardCtrl'
		}).when('/dashboard', {
			templateUrl: 'app/template/dashboard/dashboard.html',
			controller:'dashboardCtrl'
		}).when('/analyse', {
			templateUrl: 'app/template/analyse/analyse.html',
			controller:'analyseCtrl'
		}).when('/dashboard3D', {
			templateUrl: 'app/template/dashboard/dashboard3D.html',
			controller:'dashboard3DCtrl'
		}).when('/search', {
			templateUrl: 'app/template/search/search.html',
			controller:'searchCtrl'  
		}).when('/search/:param', {
			templateUrl: 'app/template/search/search.html',
			controller:'searchCtrl'  
		}).when('/usermng',{
			templateUrl:'app/template/system/usermng.html',
			controller:'usermngCtrl'
		}).when('/assets',{
			templateUrl:'app/template/assets/assets.html',
			controller:'assetsCtrl'
		}).when('/topSecurityReport',{
			templateUrl:'app/template/report/topSecurity.html',
			controller:'topSecurityReportCtrl'
		}).when('/securityReport',{
			templateUrl:'app/template/report/security.html',
			controller:'securityReportCtrl'
		}).when('/serverReport',{
			templateUrl:'app/template/report/server.html',
			controller:'serverReportCtrl'
		}).when('/complexReport',{
			templateUrl:'app/template/report/complex.html',
			controller:'complexReportCtrl'
		}).when('/node',{
			templateUrl:'app/template/cluster/node.html',
			controller:'nodeCtrl'
		}).when('/clusterDetail/:param',{
			templateUrl:'app/template/cluster/detail.html',
			controller:'clusterDetailCtrl'
		}).when('/cluster',{
			templateUrl:'app/template/cluster/cluster.html',
			controller:'clusterCtrl'
		}).when('/monitor/spark',{
			templateUrl:'app/template/cluster/spark.html',
			controller:'sparkCtrl'
		}).when('/trendLine',{
			templateUrl:'app/template/report/trendLine.html',
			controller:'trendLineCtrl'
		}).when('/confAgent/:param',{
			templateUrl:'app/template/confAgent/confAgent.html',
			controller:'confAgentCtrl'
		}).when('/addAgent/:param',{
			templateUrl:'app/template/confAgent/addAgent.html',
			controller:'addAgentCtrl'
		}).when('/detailAgent/:param1/:param2',{
			templateUrl:'app/template/confAgent/editAgent.html',
			controller:'detailConfAgentCtrl'
		}).when('/forwarder/:param',{
			templateUrl:'app/template/confForwarder/confForwarder.html',
			controller:'confForwarderCtrl'
		}).when('/forwarderAdd/:param',{
			templateUrl:'app/template/confForwarder/confForwarderAdd.html',
			controller:'confForwarderAddCtrl'
		}).when('/confForwarderDetail/:param1/:param2',{
			templateUrl:'app/template/confForwarder/confForwarderEdit.html',
			controller:'confForwarderDetailCtrl'
		}).when('/datasource/:param',{
			templateUrl:'app/template/confDatasource/confDatasource.html',
			controller:'confDataSourceCtrl'
//		}).when('/datasourceAdd/:param',{
//			templateUrl:'app/template/confDatasource/datasourceAdd2.html',
//			controller:'confDatasourceAddCtrl'
		}).when('/datasourceAdd/:param',{
			templateUrl:'app/template/confDatasource/datasourceAdd.html',
			controller:'addConfDataSourceCtrl'
		}).when('/datasourceEdit/:param1/:param2',{
			templateUrl:'app/template/confDatasource/datasourceEdit.html',
			controller:'confDatasourceEditCtrl'
		}).when('/datasourceError',{
			templateUrl:'app/template/confDatasource/datasourceError.html',
			controller:'confDatasourceErrorCtrl'
		}).when('/rolemng',{
			templateUrl:'app/template/system/rolemng.html',
			controller:'roleCtrl'
		}).when('/denied',{
			templateUrl:'app/template/denied.html'
		}).when('/systemLog',{
			templateUrl:'app/template/system/systemLog.html',
			controller:'systemLogCtrl'
		}).when('/warning',{
			templateUrl:'app/template/confWarning/confWarning.html',
			controller:'confWarningCtrl'
		}).when('/confWarningEdit/:param',{
			templateUrl:'app/template/confWarning/confWarningEdit.html',
			controller:'confWarningEditCtrl'
		}).when('/kibana',{
			templateUrl:'app/template/kibana-in.html',
			controller:'multiSearchCtrl'
		}).otherwise({
			redirectTo: '/'
		});
	}]);
	app.config(['$httpProvider', function($httpProvider) {
//	    $httpProvider.interceptors.push('loginInterceptor');
	}]);
	/**验证登录及权限**/
	app.run(function($location, $rootScope, $route,Authority,User,$q,$http) {
		$rootScope.$on('$locationChangeStart', function(evt, next, current) {
			var user = new User();
			var deferred = $q.defer();
			$http.post('tUser/isLogin.hs').success(function(data){
				if(!eval(data))
					location = "login.hs";
                deferred.resolve(data);
			}).error(function() {
                deferred.reject();
            });
			deferred.promise;
			var b = false;
			var path = $location.path();
			var auth = new Authority();
			auth.query().then(function(a){
				if(a[path] != null && a[path] != []){
					
					user.query().then(function(u){
						var b = false;
						for(var r in u.roles){
							if(a[path].indexOf(u.roles[r].name) != -1)
								b = true;
						}
						if(!b) $location.path("/denied");
					});
				}
			});
		});
	});
});