define([
        'angular',
        'lodash'
    ],
    function(angular, _) {
        'use strict';

        var module = angular.module('kibana.controllers');

        module.controller('dashLoader', function($scope, $http, timer, dashboard, alertSrv, $location) {
            $scope.loader = dashboard.current.loader;

            $scope.init = function() {
                $scope.gist_pattern = /(^\d{5,}$)|(^[a-z0-9]{10,}$)|(gist.github.com(\/*.*)\/[a-z0-9]{5,}\/*$)/;
                $scope.gist = $scope.gist || {};
                $scope.elasticsearch = $scope.elasticsearch || {};
            };

            $scope.showDropdown = function(type) {
                if (_.isUndefined(dashboard.current.loader)) {
                    return true;
                }

                var _l = dashboard.current.loader;
                if (type === 'load') {
                    return (_l.load_elasticsearch || _l.load_gist || _l.load_local);
                }
                if (type === 'save') {
                    return (_l.save_elasticsearch || _l.save_gist || _l.save_local || _l.save_default);
                }
                if (type === 'share') {
                    return (_l.save_temp);
                }
                return false;
            };

            $scope.set_default = function() {
                if (dashboard.set_default($location.path())) {
                    alertSrv.set('主页已设置', '本页已经成功设置为控制台主页', 'success', 5000);
                } else {
                    alertSrv.set('浏览器不兼容', '对不起，您的浏览器版本太老，不支持此项功能。', 'error', 5000);
                }
            };
            
            $scope.goto_default = function() {
                if (dashboard.set_default($location.path())) {
                    
                } else {
                    alertSrv.set('浏览器不兼容', '对不起，您的浏览器版本太老，不支持此项功能。', 'error', 5000);
                }
            };

            $scope.purge_default = function() {
                if (dashboard.purge_default()) {
                    alertSrv.set('缺省控制台已重置', '您的控制台已经被重置成系统缺省设置',
                        'success', 5000);
                } else {
                    alertSrv.set('浏览器不兼容', '对不起，您的浏览器版本太老，不支持此项功能。', 'error', 5000);
                }
            };

            $scope.elasticsearch_save = function(type, ttl) {
                dashboard.elasticsearch_save(
                    type, ($scope.elasticsearch.title || dashboard.current.title), ($scope.loader.save_temp_ttl_enable ? ttl : false)
                ).then(
                    function(result) {
                        if (!_.isUndefined(result._id)) {
                            alertSrv.set('控制台已保存', '本控制台已经存入Elasticsearch，名字是"' +
                                result._id + '"', 'success', 5000);
                            if (type === 'temp') {
                                $scope.share = dashboard.share_link(dashboard.current.title, 'temp', result._id);
                            }
                        } else {
                            alertSrv.set('保存失败', '本控制台无法存入Elasticsearch，请检查配置。', 'error', 5000);
                        }
                    });
            };

            $scope.elasticsearch_delete = function(id) {
                dashboard.elasticsearch_delete(id).then(
                    function(result) {
                        if (!_.isUndefined(result)) {
                            if (result.found) {
                                alertSrv.set('控制台已删除', id + ' 已经被删除', 'success', 5000);
                                // Find the deleted dashboard in the cached list and remove it
                                var toDelete = _.where($scope.elasticsearch.dashboards, {
                                    _id: id
                                })[0];
                                $scope.elasticsearch.dashboards = _.without($scope.elasticsearch.dashboards, toDelete);
                            } else {
                                alertSrv.set('找不到控制台', '无法从Elasticsearch中找到 ' + id, 'warning', 5000);
                            }
                        } else {
                            alertSrv.set('控制台删除失败', '删除控制台时发生错误', 'error', 5000);
                        }
                    }
                );
            };

            $scope.elasticsearch_dblist = function(query) {
                dashboard.elasticsearch_list(query, $scope.loader.load_elasticsearch_size).then(
                    function(result) {
                        if (!_.isUndefined(result.hits)) {
                            $scope.hits = result.hits.total;
                            $scope.elasticsearch.dashboards = result.hits.hits;
                        }
                    });
            };

            $scope.save_gist = function() {
                dashboard.save_gist($scope.gist.title).then(
                    function(link) {
                        if (!_.isUndefined(link)) {
                            $scope.gist.last = link;
                            alertSrv.set('Gist saved', 'You will be able to access your exported dashboard file at ' +
                                '<a href="' + link + '">' + link + '</a> in a moment', 'success');
                        } else {
                            alertSrv.set('Save failed', 'Gist could not be saved', 'error', 5000);
                        }
                    });
            };

            $scope.gist_dblist = function(id) {
                dashboard.gist_list(id).then(
                    function(files) {
                        if (files && files.length > 0) {
                            $scope.gist.files = files;
                        } else {
                            alertSrv.set('Gist Failed', 'Could not retrieve dashboard list from gist', 'error', 5000);
                        }
                    });
            };

        });

    });