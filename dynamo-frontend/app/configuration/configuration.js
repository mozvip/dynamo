'use strict';

angular.module('dynamo.configuration', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/configuration', {
            templateUrl: 'configuration/configuration.html',
            controller: 'ConfigurationCtrl',
            resolve: {
                configuration: ['configurationService', function (configurationService) {
                    return configurationService.getItems();
                }]
            }
        }).when('/quality-profiles', {
            templateUrl: 'configuration/quality-profiles.html',
            controller: 'QualityProfilesCtrl'
        }).when('/plugins', {
            templateUrl: 'configuration/plugins.html',
            controller: 'PluginsCtrl',
            resolve: {
                configuration: ['configurationService', function (configurationService) {
                    return configurationService.getItems();
                }],
                pluginOptions: ['BackendService', function(  BackendService  ) {
                    return BackendService.get('configuration/plugin-options');
                }]
            }
        });
    }])

    .controller('PluginsCtrl', ['$scope', 'configurationService', '$filter', 'pluginOptions', 'configuration', function ($scope, configurationService, $filter, pluginOptions, configuration) {

        $scope.pluginOptions = pluginOptions.data;
        $scope.config = configuration.data;

        $scope.pluginChanged = function() {
            $scope.pluginOptions.forEach(function(plugin) {
                plugin.itemsToConfigure = [];
                var configurationKeys = Object.keys($scope.config).filter( function( key ) {
                    return plugin.value && $scope.config[key].category == plugin.value;
                });
                configurationKeys.forEach(function(key) {
                    plugin.itemsToConfigure.push( $scope.config[key] );  
                }, this);
            }, this);
        }

        $scope.saveSettings = function () {
            var itemsToSave = [];
            $scope.pluginOptions.forEach(function(plugin) {
                itemsToSave.push({'key':'Plugin.' + plugin.taskClass.klass, 'value': plugin.value});
                if (plugin.itemsToConfigure) {
                    itemsToSave = itemsToSave.concat(plugin.itemsToConfigure);
                }
            }, this);
            configurationService.saveItems( itemsToSave );
        }        

        $scope.pluginChanged();

    }])

    .controller('ConfigurationCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

        $scope.config = configuration.data;

        $scope.itemsToConfigure = [
            $scope.config['EZTVProvider.enabled'],
            $scope.config['EZTVProvider.baseURL'],
            $scope.config['KATProvider.enabled'],
            $scope.config['KATProvider.baseURL'],
            $scope.config['T411Provider.enabled'],
            $scope.config['T411Provider.baseURL'],
            $scope.config['T411Provider.login'],
            $scope.config['T411Provider.password'],
            $scope.config['UsenetCrawlerProvider.enabled'],
            $scope.config['UsenetCrawlerProvider.login'],
            $scope.config['UsenetCrawlerProvider.password']
            
        ];

        $scope.saveSettings = function () {
            configurationService.saveItems( $scope.itemsToConfigure );
        }

    }])

    .controller('QualityProfilesCtrl', ['$scope', 'configurationService', '$filter', function ($scope, configurationService, $filter) {

        $scope.items = {};

        configurationService.getItems().then(function (response) {
            $scope.items = response.data;
        });

        $scope.configurationChanged = function (key) {
            configurationService.set(key, $scope.items[key].value);
        }

    }]);
