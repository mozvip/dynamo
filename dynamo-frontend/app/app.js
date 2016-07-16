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

  $scope.magazinesCollectionCount = 0;
  $scope.magazinesWantedCount = 0;
  $scope.magazinesSuggestionCount = 0;

  $scope.booksCollectionCount = 0;
  $scope.booksWantedCount = 0;
  $scope.booksSuggestionCount = 0;

  $scope.musicAlbumsCollectionCount = 0;
  $scope.musicAlbumsWantedCount = 0;
  $scope.musicAlbumsSuggestionCount = 0;

  downloadableService.counts().then( function(response) {

    var counts = response.data;
    for(var i=0; i<counts.length; i++) {
      if (counts[i].type == 'Movie') {
        if (counts[i].status == 'DOWNLOADED') {
          $scope.moviesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $scope.moviesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $scope.moviesSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'MagazineIssue') {
        if (counts[i].status == 'DOWNLOADED') {
          $scope.magazinesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $scope.magazinesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $scope.magazinesSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'Book') {
        if (counts[i].status == 'DOWNLOADED') {
          $scope.booksCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $scope.booksWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $scope.booksSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'MusicAlbum') {
        if (counts[i].status == 'DOWNLOADED') {
          $scope.musicAlbumsCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $scope.musicAlbumsWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $scope.musicAlbumsSuggestionCount = counts[i].count;
        }
      }
      
    }

    // TODO

  });
}]);
