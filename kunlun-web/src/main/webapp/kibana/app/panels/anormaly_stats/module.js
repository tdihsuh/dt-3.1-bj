/*

  ## Stats Module

  ### Parameters
  * format :: The format of the value returned. (Default: number)
  * style :: The font size of the main number to be displayed.
  * mode :: The aggergate value to use for display
  * spyable ::  Dislay the 'eye' icon that show the last elasticsearch query

*/
define([
    'angular',
    'app',
    'lodash',
    'jquery',
    'kbn',
    'numeral'
], function(
    angular,
    app,
    _,
    $,
    kbn,
    numeral
) {

    'use strict';

    var module = angular.module('kibana.panels.anormaly_stats', []);
    app.useModule(module);

    module.controller('anormaly_stats', function($scope, querySrv, dashboard, filterSrv) {

        $scope.panelMeta = {
            modals: [{
                description: "查看",
                icon: "icon-info-sign",
                partial: "app/partials/inspector.html",
                show: $scope.panel.spyable
            }],
            editorTabs: [{
                title: '查询',
                src: 'app/partials/querySelect.html'
            }],
            status: 'Beta',
            description: '统计异常事件总数'
        };

        $scope.modes = ['count', 'min', 'max', 'mean', 'total', 'variance', 'std_deviation', 'sum_of_squares'];

        var defaults = {
            queries: {
                mode: 'all',
                ids: []
            },
            style: {
                "font-size": '24pt'
            },
            format: 'number',
            mode: 'count',
            display_breakdown: 'yes',
            sort_field: '',
            sort_reverse: false,
            label_name: 'Query',
            value_name: 'Value',
            spyable: true,
            show: {
                count: true,
                min: true,
                max: true,
                mean: true,
                std_deviation: true,
                sum_of_squares: true,
                total: true,
                variance: true
            }
        };

        _.defaults($scope.panel, defaults);

        $scope.init = function() {
            $scope.ready = false;
            $scope.$on('refresh', function() {
                $scope.get_data();
            });
            $scope.get_data();
        };

        $scope.set_sort = function(field) {
            console.log(field);
            if ($scope.panel.sort_field === field && $scope.panel.sort_reverse === false) {
                $scope.panel.sort_reverse = true;
            } else if ($scope.panel.sort_field === field && $scope.panel.sort_reverse === true) {
                $scope.panel.sort_field = '';
                $scope.panel.sort_reverse = false;
            } else {
                $scope.panel.sort_field = field;
                $scope.panel.sort_reverse = false;
            }
        };

        $scope.get_data = function() {
            if (dashboard.indices.length === 0) {
                return;
            }

            $scope.panelMeta.loading = true;

            var request,
                results,
                boolQuery,
                queries;

            var ids = ["anormal"];
            request = $scope.ejs.Request().indices(ids);

            var cf = null;
            var f = filterSrv.ids();
            if (f.length >= 1) {
                f = filterSrv.getEjsObj(f[0]);
                if (f._type() === "range") {
                    // var from = f.from(null);
                    // var to = f.to(null);
                    cf = _.clone(f).field("startDatetime");
                }
            }

            if (!cf) {
                cf = ejs.MatchAllFilter();
            }

            request = request
                .facet($scope.ejs.StatisticalFacet('stats')
                    .field($scope.panel.field)
                    .facetFilter(cf)).size(0);

            // Populate the inspector panel
            $scope.inspector = angular.toJson(JSON.parse(request.toString()), true);

            results = request.doSearch();

            results.then(function(results) {
                $scope.panelMeta.loading = false;
                var value = results.facets.stats[$scope.panel.mode];

                var obj = new Object();
                obj.label = "*"
                obj.Label = "*"; //sort field
                obj.value = results.facets['stats'];
                obj.Value = results.facets['stats']; //sort field

                var rows = [obj];

                $scope.data = {
                    value: value,
                    rows: rows
                };

                console.log($scope.data);

                $scope.$emit('render');
            });
        };

        $scope.set_refresh = function(state) {
            $scope.refresh = state;
        };

        $scope.close_edit = function() {
            if ($scope.refresh) {
                $scope.get_data();
            }
            $scope.refresh = false;
            $scope.$emit('render');
        };

    });

    module.filter('formatstats', function() {
        return function(value, format) {
            switch (format) {
                case 'money':
                    value = numeral(value).format('$0,0.00');
                    break;
                case 'bytes':
                    value = numeral(value).format('0.00b');
                    break;
                case 'float':
                    value = numeral(value).format('0.000');
                    break;
                default:
                    value = numeral(value).format('0,0');
            }
            return value;
        };
    });

});