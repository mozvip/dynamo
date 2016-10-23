'use strict';

angular.module('dynamo.trakt', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/configuration-video', {
    templateUrl: 'configuration/configuration-video.html',
    controller: 'ConfigurationVideoCtrl',
    resolve: {
      configuration: ['configurationService', function(  configurationService  ) {
        return configurationService.getItems();
      }]
    }
  });
}])

.controller('ConfigurationVideoCtrl', ['$scope', 'configuration', 'configurationService', 'BackendService', function( $scope, configuration, configurationService, BackendService ) {

  $scope.authorizeURL = 'https://trakt.tv/oauth/authorize?client_id=1f93ab28686a87d36c0e198f15a34ba7c0d3fb45cbd3303515da246718b570a6&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&response_type=code';

  $scope.config = configuration.data;

  $scope.itemsToConfigure = [
      $scope.config['VideoManager.mediaInfoBinaryPath'],
      $scope.config['TraktManager.enabled'],
      $scope.config['TraktManager.username']
  ];

  $scope.traktPinCode = '';

  $scope.saveSettings = function () {
    BackendService.post('trakt/auth/' + $scope.traktPinCode);
    configurationService.saveItems($scope.itemsToConfigure);
  }

}]);
