'use strict';

angular.module('dynamo.trakt', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/trakt', {
    templateUrl: 'configuration/trakt.html',
    controller: 'TraktCtrl',
    resolve: {
      configuration: ['configurationService', function(  configurationService  ) {
        return configurationService.getItems();
      }]
    }
  });
}])

.factory('traktService', ['BackendService', function(BackendService){
  var traktService = {};

  return traktService;
}])

.controller('TraktCtrl', ['$scope', 'traktService', 'configuration', 'BackendService', function( $scope, traktService, configuration, BackendService ) {

  $scope.authorizeURL = 'https://trakt.tv/oauth/authorize?client_id=1f93ab28686a87d36c0e198f15a34ba7c0d3fb45cbd3303515da246718b570a6&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&response_type=code';

}]);
