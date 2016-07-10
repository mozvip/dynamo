'use strict';

// Declare app level module which depends on views, and components
angular.module('dynamo', [
  'ngRoute',
  'ngResource',
  'ngWebSocket',
  'ui.bootstrap',
  'ng-sweet-alert',
  'dynamo.common',
  'dynamo.log',
  'dynamo.home',
  'dynamo.configuration',
  'dynamo.movies',
  'dynamo.music',
  'dynamo.books',
  'dynamo.magazines',
  'dynamo.tvshows'
])

.constant('backendHostAndPort', 'localhost:8081')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/welcome'});
}])
.controller('MenuCtrl', ['$scope', 'eventDataService', 'downloadableService', function($scope, eventDataService, downloadableService) {

  $scope.moviesCollectionCount = 0;
  $scope.moviesWantedCount = 0;
  $scope.moviesSuggestionCount = 0;

  downloadableService.counts().then( function(response) {

    var counts = response.data;
    for(var i=0; i<counts.length; i++) {
      if (counts[i].type == 'Movie') {
        if (counts[i].status == 'DOWNLOADED') {
          $scope.moviesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'DOWNLOADED' || counts[i].status == 'DOWNLOADED') {
          $scope.moviesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $scope.moviesSuggestionCount = counts[i].count;
        }

      }
    }

    // TODO

  });
}]);
