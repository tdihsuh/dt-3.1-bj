/** @scratch /panels/5
 *
 * include::panels/map.asciidoc[]
 */

/** @scratch /panels/map/0
 *
 * == Map
 * Status: *Stable*
 *
 * The map panel translates 2 letter country or state codes into shaded regions on a map. Currently
 * available maps are world, usa and europe.
 *
 */
define([
        'angular',
        'app',
        'lodash',
        'jquery',
        'config',
        './lib/jquery.jvectormap.min'
    ],
    function(angular, app, _, $, config) {
        'use strict';

        var module = angular.module('kibana.panels.ip_map', []);
        app.useModule(module);

        module.controller('ip_map', function($scope, querySrv, dashboard, filterSrv, $http) {
            $scope.panelMeta = {
                editorTabs: [{
                    title: '查询',
                    src: 'app/partials/querySelect.html'
                }],
                modals: [{
                    description: "查看",
                    icon: "icon-info-sign",
                    partial: "app/partials/inspector.html",
                    show: $scope.panel.spyable
                }],
                status: "Beta",
                description: "IP分布地图"
            };

            // Set and populate defaults
            var _d = {
                /** @scratch /panels/map/3
                 *
                 * === Parameters
                 *
                 * map:: Map to display. world, usa, europe
                 */
                map: "china",
                /** @scratch /panels/map/3
                 * colors:: An array of colors to use to shade the map. If 2 colors are specified, shades
                 * between them will be used.For example[`#A0E2E2', `                #265656']
                 */
                colors: ['#CEECF5', '#0404B4'],
                /** @scratch /panels/map/3
                 * size:: Max number of regions to shade
                 */
                size: 100,
                /** @scratch /panels/map/3
                 * exclude:: exclude this array of regions. For example [`US',`BR',`IN']
                 */
                exclude: [],
                /** @scratch /panels/map/3
                 * spyable:: Setting spyable to false disables the inspect icon.
                 */
                spyable: true,
                /** @scratch /panels/map/5
                 *
                 * ==== Queries
                 * queries object:: This object describes the queries to use on this panel.
                 * queries.mode::: Of the queries available, which to use. Options: +all, pinned, unpinned, selected+
                 * queries.ids::: In +selected+ mode, which query ids are selected.
                 */
                queries: {
                    mode: 'all',
                    ids: []
                }
            };
            _.defaults($scope.panel, _d);

            $scope.init = function() {
                $scope.$on('refresh', function() {
                    $scope.get_data();
                });
                $scope.get_data();
            };

            $scope.get_data = function() {

                // Make sure we have everything for the request to complete
                if (dashboard.indices.length === 0) {
                    return;
                }
                $scope.panelMeta.loading = true;


                var request,
                    boolQuery,
                    queries;

                $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);
                request = $scope.ejs.Request().indices(dashboard.indices);
                queries = querySrv.getQueryObjs($scope.panel.queries.ids);

                boolQuery = $scope.ejs.BoolQuery();
                _.each(queries, function(q) {
                    boolQuery = boolQuery.should(querySrv.toEjsObj(q));
                });

                // Then the insert into facet and make the request
                request = request
                    .facet($scope.ejs.TermsFacet('map')
                        .field($scope.panel.field)
                        .size($scope.panel.size)
                        .exclude($scope.panel.exclude)
                        .facetFilter($scope.ejs.QueryFilter(
                            $scope.ejs.FilteredQuery(
                                boolQuery,
                                filterSrv.getBoolFilter(filterSrv.ids())
                            )))).size(0);

                $scope.populate_modal(request);

                var results = request.doSearch();

                // Populate scope when we have results
                results.then(function(results) {
                    $scope.hits = results.hits.total;

                    var ip2count = {};
                    _.each(results.facets.map.terms, function(v) {
                        ip2count[v.term] = v.count;
                    });

                    var ip2loc = {};
                    for (var ip in ip2count) {
                        ip2loc[ip] = 0;
                    }

                    // convert IP to regions by looking up GeoIP online db
                    var result = $http({
                        url: window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/kibana/servlet/geoipquery',
                        method: "POST",
                        data: ip2loc
                    });

                    $scope.panelMeta.loading = false;

                    result.then(function(result) {
                        ip2loc = result.data;

                        var loc2count = {};
                        for (var ip in ip2loc) {
                            var loc = ip2loc[ip];
                            if (loc == undefined) continue;

                            var country = loc.country;
                            if (country == undefined) continue;

                            var prov = loc.city;
                            // TODO rename city to province
                            if (prov == undefined || $scope.panel.map === "world") {
                                prov = country;
                            } else {
                                prov = country + '_' + prov;
                            }

                            var count = ip2count[ip];

                            var oldCount = loc2count[prov];
                            if (oldCount == undefined) {
                                oldCount = 0;
                            }
                            oldCount += count;
                            loc2count[prov] = oldCount;
                        }

                        $scope.data = loc2count;
                        $scope.$emit('render');
                    });
                });
            };

            // I really don't like this function, too much dom manip. Break out into directive?
            $scope.populate_modal = function(request) {
                $scope.inspector = angular.toJson(JSON.parse(request.toString()), true);
            };

            $scope.build_search = function(field, value) {
                filterSrv.set({
                    type: 'field',
                    field: field,
                    query: value,
                    mandate: "must"
                });
            };

        });


        module.directive('map', function() {
            return {
                restrict: 'A',
                link: function(scope, elem) {

                    elem.html('<center><img src="img/load_big.gif"></center>');

                    // Receive render events
                    scope.$on('render', function() {
                        slow();
                    });

                    elem.closest('.panel').resize(function() {
                        elem.empty();
                    });

                    function render_panel() {
                        elem.empty();
                        elem.css({
                            height: scope.panel.height || scope.row.height
                        });
                        $('.jvectormap-zoomin,.jvectormap-zoomout,.jvectormap-label').remove();
                        require(['./panels/map/lib/map.' + scope.panel.map], function() {
                            elem.vectorMap({
                                map: scope.panel.map,
                                regionStyle: {
                                    initial: {
                                        fill: '#cccccc'
                                    }
                                },
                                zoomOnScroll: false,
                                backgroundColor: null,
                                series: {
                                    regions: [{
                                        values: scope.data,
                                        scale: scope.panel.colors,
                                        normalizeFunction: 'polynomial'
                                    }]
                                },
                                onRegionLabelShow: function(event, label, code) {
                                    elem.children('.map-legend').show();
                                    var count = _.isUndefined(scope.data[code]) ? 0 : scope.data[code];
                                    elem.children('.map-legend').text(label.text() + ": " + count);
                                },
                                onRegionOut: function() {
                                    elem.children('.map-legend').hide();
                                },
                                onRegionClick: function(event, code) {
                                    var count = _.isUndefined(scope.data[code]) ? 0 : scope.data[code];
                                    if (count !== 0) {
                                        scope.build_search(scope.panel.field, code);
                                    }
                                }
                            });
                            elem.prepend('<span class="map-legend"></span>');

                            elem.children('.map-legend').hide();
                        });
                    }

                    var slow = _.debounce(render_panel, 200);
                }
            };
        });
    });