'use strict';

// Declare app level module which depends on views, and components
angular.module('dynamo', [
  'angular.filter',
  'ngRoute',
  'ngResource',
  'ngSanitize',
  'ngWebSocket',
  'ui.bootstrap',
  'ng-sweet-alert',
  'dynamo.common',
  'dynamo.trakt',
  'dynamo.log',
  'dynamo.home',
  'dynamo.configuration',
  'dynamo.movies',
  'dynamo.music',
  'dynamo.books',
  'dynamo.magazines',
  'dynamo.tvshows',
  'dynamo.games'
])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/welcome'});
}])
.controller('MenuCtrl', ['$scope', '$rootScope', 'eventDataService', 'downloadableService', function($scope, $rootScope, eventDataService, downloadableService) {

  $rootScope.moviesCollectionCount = 0;
  $rootScope.moviesWantedCount = 0;
  $rootScope.moviesSuggestionCount = 0;

  $rootScope.magazinesCollectionCount = 0;
  $rootScope.magazinesWantedCount = 0;
  $rootScope.magazinesSuggestionCount = 0;

  $rootScope.booksCollectionCount = 0;
  $rootScope.booksWantedCount = 0;
  $rootScope.booksSuggestionCount = 0;

  $rootScope.gamesCollectionCount = 0;
  $rootScope.gamesWantedCount = 0;
  $rootScope.gamesSuggestionCount = 0;

  $rootScope.musicAlbumsCollectionCount = 0;
  $rootScope.musicAlbumsWantedCount = 0;
  $rootScope.musicAlbumsSuggestionCount = 0;

  downloadableService.counts().then( function(response) {

    var counts = response.data;
    for(var i=0; i<counts.length; i++) {
      if (counts[i].type == 'Movie') {
        if (counts[i].status == 'DOWNLOADED') {
          $rootScope.moviesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $rootScope.moviesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $rootScope.moviesSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'MagazineIssue') {
        if (counts[i].status == 'DOWNLOADED') {
          $rootScope.magazinesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $rootScope.magazinesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $rootScope.magazinesSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'Book') {
        if (counts[i].status == 'DOWNLOADED') {
          $rootScope.booksCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $rootScope.booksWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $rootScope.booksSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'VideoGame') {
        if (counts[i].status == 'DOWNLOADED') {
          $rootScope.gamesCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $rootScope.gamesWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $rootScope.gamesSuggestionCount = counts[i].count;
        }
      }
      if (counts[i].type == 'MusicAlbum') {
        if (counts[i].status == 'DOWNLOADED') {
          $rootScope.musicAlbumsCollectionCount = counts[i].count;
        } else if (counts[i].status == 'SNATCHED' || counts[i].status == 'WANTED') {
          $rootScope.musicAlbumsWantedCount += counts[i].count;
        } else if (counts[i].status == 'SUGGESTED') {
          $rootScope.musicAlbumsSuggestionCount = counts[i].count;
        }
      }
      
    }

    // TODO

  });
}]);
