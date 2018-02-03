require.config({
	baseUrl:'app',
	paths:{
		'angular':'../vendor/angular/angular.min',
		'angular-route':'../vendor/angular/angular-route.min',
		'angular-sanitize':'../vendor/angular/angular-sanitize.min',
		'angular-resource':'../vendor/angular/angular-resource.min',
		jquery:'../vendor/jquery/jquery-2.1.1.min',
		'jquery-ui':'../vendor/jquery/jquery-ui-1.10.4.min',
		'jquery-ui-datepicker':'../vendor/jquery/jquery.ui.datepicker-zh-TW',
		domReady:'../vendor/require/domReady',
		highcharts:'../vendor/highcharts/highcharts',
		'highcharts-3d':'../vendor/highcharts/highcharts-3d',
		exporting:'../vendor/highcharts/exporting',
		data:'../vendor/highcharts/data',
		bootstrap:'../vendor/bootstrap-3.2.0-dist/js/bootstrap.min',
		numeral:'../vendor/numeral',
		lodash:'../vendor/lodash',
		'openlayers':'../vendor/openlayers/OpenLayers',
		'angular-easypiechart':'../vendor/angular/angular.easypiechart.min',
		d3:'../vendor/d3/d3.min',
		'qtip2':'../vendor/jquery/jquery.tooltipster.min',
		wizard:'../vendor/plugin/jquery.bootstrap.wizard',
		socketio:'../vendor/socketio/socket.io-1.0.6', 
		'spectrum':'../vendor/spectrum/spectrum',
		'moment':'../vendor/moment'
	},
	shim:{
		jquery: {
            exports: "jQuery"
        },
		"jquery-ui": ["jquery"],
		'jquery-ui-datepicker':["jquery-ui"],
		'angular':{
			exports:'angular',
			depts:'jquery'
		},
		'angular-route':["angular"],
		'angular-sanitize':["angular"],
		'angular-resource':["angular"],
		highcharts:["jquery"],
		'highcharts-3d':["highcharts"],
		exporting:["highcharts"],
		data:["highcharts","highcharts-3d"],
		bootstrap:["jquery"],
		'scripts/controllers/dashboardCtrl':['openlayers'],
		'qtip2':["jquery"],
		wizard:["jquery"],
		'spectrum':["jquery"],
		app:['angular-route','angular-sanitize','angular-resource','jquery-ui','exporting','bootstrap','data','jquery-ui-datepicker','openlayers','angular-easypiechart','qtip2','moment']
	}
});
require([
	'angular',
	'app',
	'scripts/common/variable',
	'scripts/common/charts',
	/**'scripts/jqconsole/jqconsole',**/
	
	'scripts/charts/nodeCharts',
	'scripts/charts/trendLineCharts',
	
	'scripts/controllers/menuCtrl',
	'scripts/controllers/dashboardCtrl',
	'scripts/controllers/analyseCtrl',
	'scripts/controllers/dashboard3DCtrl',
	'scripts/controllers/searchCtrl',
	'scripts/controllers/assetsCtrl',
	'scripts/controllers/topSecurityReportCtrl',
	'scripts/controllers/securityReportCtrl',
	'scripts/controllers/serverReportCtrl',
	'scripts/controllers/complexReportCtrl',
	'scripts/controllers/clusterCtrl',
	'scripts/controllers/sparkCtrl',
	'scripts/controllers/nodeCtrl',
	'scripts/controllers/clusterDetailCtrl',
	'scripts/controllers/trendLineCtrl',
	'scripts/controllers/confAgentCtrl',


	'scripts/controllers/panelCtrl',
	'scripts/controllers/sparkshellCtrl',

	'scripts/controllers/addAgentCtrl',
	'scripts/controllers/detailConfAgentCtrl',
	'scripts/controllers/confForwarderCtrl',
	'scripts/controllers/confForwarderAddCtrl',
	'scripts/controllers/confForwarderDetailCtrl',
	
	'scripts/controllers/confDataSourceCtrl',
	//'scripts/controllers/confDatasourceAddCtrl',
	'scripts/controllers/addConfDataSourceCtrl',
	'scripts/controllers/confDatasourceEditCtrl',
	
	'scripts/controllers/confWarningCtrl',
	'scripts/controllers/informationCtrl',
	
	'scripts/controllers/system/userCtrl',
	'scripts/controllers/system/usermngCtrl',
	'scripts/controllers/system/roleCtrl',

	'scripts/controllers/systemLogCtrl',
	'scripts/controllers/multiSearchCtrl',
	
	'scripts/providers/providers',
	'scripts/directives/directives',
	'scripts/common/times',
	'scripts/common/commons',
	'scripts/common/hsmap',
	'scripts/common/popup',
	'scripts/factories/role',
	'scripts/factories/authority',
	'scripts/factories/user',
	'scripts/factories/interceptors/LoginInterceptor'
],function(angular){
	"use strict";
	angular.bootstrap(document, ['hansight']);
	jQuery.expr[':'].icontains = function(a, i, m) {
	  return jQuery(a).text().toUpperCase()
	      .indexOf(m[3].toUpperCase()) >= 0;
	};
});
