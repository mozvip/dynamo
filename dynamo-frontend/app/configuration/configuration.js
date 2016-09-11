'use strict';

angular.module('dynamo.configuration', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/configuration-providers', {
            templateUrl: 'configuration/configuration.html',
            controller: 'ProvidersCtrl',
            resolve: {
                configuration: ['configurationService', function (configurationService) {
                    return configurationService.getItems();
                }]
            }
        }).when('/configuration-notifications', {
            templateUrl: 'configuration/configuration.html',
            controller: 'NotificationsConfigCtrl',
            resolve: {
                configuration: ['configurationService', function (configurationService) {
                    return configurationService.getItems();
                }]
            }
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

        $scope.pluginOptions.forEach(function(plugin) {
            plugin.executorOptions.push({'label': 'None', 'klass': undefined});
        }, this);

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

    .controller('ConfigurationCtrl', ['$scope', 'configurationService', 'itemsToConfigure', function ($scope, configurationService, itemsToConfigure) {

        $scope.itemsToConfigure = itemsToConfigure.data;

        $scope.saveSettings = function () {
            configurationService.saveItems( $scope.itemsToConfigure );
        }

    }])

    .controller('NotificationsConfigCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

        $scope.config = configuration.data;

        $scope.itemsToConfigure = [
            $scope.config['DownloadableManager.notifyOnSnatch'],
            $scope.config['DownloadableManager.notifyOnDownload'],
            $scope.config['PushBullet.accessToken'],
            $scope.config['PushBullet.deviceIdent']
        ];

        $scope.saveSettings = function () {
            configurationService.saveItems( $scope.itemsToConfigure );
        }

    }])    

    .controller('ProvidersCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

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
            $scope.config['CPasBienProvider.enabled'],
            $scope.config['RARBGProvider.enabled'],
            $scope.config['PirateBayProvider.enabled'],
            $scope.config['TorrentProjectSE.enabled'],
            $scope.config['UsenetCrawlerProvider.enabled'],
            $scope.config['UsenetCrawlerProvider.login'],
            $scope.config['UsenetCrawlerProvider.password'],
            $scope.config['NZBIndexNLProvider.enabled'],
            $scope.config['BTDigg.enabled']
        ];

        $scope.saveSettings = function () {
            configurationService.saveItems( $scope.itemsToConfigure );
        }

    }]);
