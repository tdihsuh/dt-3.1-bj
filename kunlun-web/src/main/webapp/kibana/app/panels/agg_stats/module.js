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

    var module = angular.module('kibana.panels.agg_stats', []);
    app.useModule(module);

    module.controller('agg_stats', function($scope, querySrv, dashboard, filterSrv) {

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
            description: '从查询值中选择值进行统计'
        };

        $scope.modes = ['50.0', '75.0', '95.0', '99.0'];

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

            request = $scope.ejs.Request().indices(dashboard.indices);

            $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);
            queries = querySrv.getQueryObjs($scope.panel.queries.ids);


            // This could probably be changed to a BoolFilter
            boolQuery = $scope.ejs.BoolQuery();
            _.each(queries, function(q) {
                boolQuery = boolQuery.should(querySrv.toEjsObj(q));
            });

            request = request
                .aggregation($scope.ejs.FilterAggregation('stats')
                    .agg($scope.ejs.PercentilesAggregation('stats_agg')
                        .field($scope.panel.field)
                        .percents($scope.modes))
                    .filter($scope.ejs.QueryFilter(
                        $scope.ejs.FilteredQuery(
                            boolQuery,
                            filterSrv.getBoolFilter(filterSrv.ids())
                        )))).size(0);

            _.each(queries, function(q) {
                var alias = q.alias || q.query;
                var query = $scope.ejs.BoolQuery();
                query.should(querySrv.toEjsObj(q));
                alias = alias.replace(/[\*\?\:\,]/g, "_")
                request.aggregation($scope.ejs.FilterAggregation('stats_' + alias)
                    .agg($scope.ejs.PercentilesAggregation('stats_agg')
                        .field($scope.panel.field)
                        .percents($scope.modes))
                    .filter($scope.ejs.QueryFilter(
                        $scope.ejs.FilteredQuery(
                            query,
                            filterSrv.getBoolFilter(filterSrv.ids())
                        )
                    ))
                );
            });

            // Populate the inspector panel
            $scope.inspector = angular.toJson(JSON.parse(request.toString()), true);

            results = request.doSearch();

            results.then(function(results) {
                $scope.panelMeta.loading = false;
                //var value = results.aggregations.stats[$scope.panel.mode];
                var value = results.aggregations.stats.stats_agg.values[$scope.panel.mode];

                var rows = queries.map(function(q, i) {
                    var alias = q.alias || q.query;
                    var obj = _.clone(q);
                    obj.label = alias;
                    obj.Label = alias.toLowerCase(); //sort field
                    alias = alias.replace(/[\*\?\:\,]/g, "_")
                    obj.value = results.aggregations['stats_' + alias].stats_agg.values;
                    obj.Value = results.aggregations['stats_' + alias].stats_agg.values; //sort field
                    return obj;
                });

                $scope.data = {
                    value: value,
                    rows: rows
                };

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