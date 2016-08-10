'use strict';

angular.module('dynamo.tvshows', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/tvshows', {
    templateUrl: 'tvshows/tvshows.html',
    controller: 'TVShowsCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      unrecognizeds: ['tvShowsService', function( tvShowsService ) {
        return tvShowsService.getUnrecognized();
      }]
    }
  }).when('/tvshows-add', {
    templateUrl: 'tvshows/tvshows-add.html',
    controller: 'TVShowsAddCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }]      
    }
  }).when('/tvshows-unrecognized', {
    templateUrl: 'tvshows/tvshows-unrecognized.html',
    controller: 'TVShowsUnrecognizedCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      unrecognizeds: ['tvShowsService', function( tvShowsService ) {
        return tvShowsService.getUnrecognized();
      }]
    }
  }).when('/tvshow-detail/:tvShowId', {
    templateUrl: 'tvshows/tvshow-detail.html',
    controller: 'TVShowDetailsCtrl',
    resolve: {
      tvshow: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getTVShow( $route.current.params.tvShowId );
      }],
      episodes: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getEpisodes( $route.current.params.tvShowId );
      }],
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      unrecognizedFiles: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getUnrecognizedFiles( $route.current.params.tvShowId );
      }]      
    }
  });
}])

.factory('tvdbService', ['BackendService', function(BackendService){
  var tvdbService = {};
  tvdbService.find = function( title ) {
    return BackendService.get('tvdb/search', {'title' : title, 'language' : 'EN'});
  }
  tvdbService.find = function( title, year, language ) {
    var searchData = { 'title' : title, 'language': 'EN' };
    if (year) {
      searchData.year = year;
    }
    if (language) {
      searchData.language = language;
    }
    return BackendService.get('tvdb/search', searchData);
  }
  return tvdbService;
}])

.factory('tvShowsService', ['BackendService', function(BackendService){
  var tvShowsService = {};
  tvShowsService.find = function() {
    return BackendService.get('tvshows');
  }
  tvShowsService.getTVShow = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId);
  }
  tvShowsService.getFolders = function() {
    return BackendService.get('tvshows/folders');
  }
  tvShowsService.getEpisodes = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId + '/episodes');
  }
  tvShowsService.rescan = function( tvshowId ) {
    return BackendService.post('tvshows/rescan/' + tvshowId);
  }
  tvShowsService.getUnrecognized = function() {
    return BackendService.get('tvshows/unrecognized');
  }
  tvShowsService.getUnrecognizedFiles = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId + '/unrecognized');
  }
  return tvShowsService;
}])

.controller('TVShowDetailsCtrl', ['$scope', 'tvshow', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'languages', 'episodes', 'unrecognizedFiles', function( $scope, tvshow, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal, filterFilter, languages, episodes, unrecognizedFiles ) {

  $scope.tvshow = tvshow.data;
  $scope.episodes = episodes.data;
  $scope.languages = languages.data;
  $scope.unrecognizedFiles = unrecognizedFiles.data;

  $scope.want = function( episode ) {
    downloadableService.want( episode.id ).then( function( response ) {
      episode.status = 'WANTED';
    } );
  }

  $scope.rescan = function( tvshow ) {
    tvShowsService.rescan( tvshow.id );
  }

  $scope.openFileList = function ( downloadable) {
    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

}])

.controller('TVShowsUnrecognizedCtrl', ['$scope', 'tvShowsService', 'tvdbService', '$uibModal', 'filterFilter', 'languages', 'unrecognizeds', function( $scope, tvShowsService, tvdbService, $uibModal, filterFilter, languages, unrecognizeds ) {

  $scope.unrecognizeds = unrecognizeds.data;
  $scope.languages = languages.data;

}])

.controller('TVShowsAddCtrl', ['$scope', '$location', 'tvShowsService', 'tvdbService', 'BackendService', 'languages', function( $scope, $location, tvShowsService, tvdbService, BackendService, languages ) {

  $scope.languages = languages.data;

  $scope.language = 'EN';

  $scope.results = [];
  $scope.searchTVShow = function() {
    tvdbService.find( $scope.title, $scope.year, $scope.language ).then( function( response ) {
      $scope.results = response.data;
    });
  }

  $scope.audioLanguage = 'EN';
  $scope.subTitlesLanguage = 'FR';
  $scope.selectedTVShow = undefined;

  $scope.selectTVShow = function( tvshow ) {
    $scope.audioLanguage = tvshow.language.toUpperCase();
    $scope.selectedTVShow = tvshow;
  }

  $scope.addTVShow = function( id ) {
    BackendService.post("tvshows/add", {"id": id, 'seriesName': $scope.selectedTVShow.seriesName, 'audioLanguage': $scope.audioLanguage, 'subtitlesLanguage': $scope.subtitlesLanguage}).then(
      function( response ) {
        $location.path("/tvshow-detail/" + response.data);
      }
    );
  }

}])

.controller('TVShowsCtrl', ['$scope', '$routeParams', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'BackendService', 'languages', 'unrecognizeds', function( $scope, $routeParams, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal,  filterFilter, BackendService, languages, unrecognizeds ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.unrecognizeds = unrecognizeds.data;

  $scope.languages = languages.data;

  $scope.pageContents = [];
  tvShowsService.find().then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, 24 );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.want = function( downloadable ) {
    downloadableService.want( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.redownload = function( downloadable ) {
    downloadableService.redownload( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * 24;
    $scope.pageContents = $scope.filteredList.slice( start, start + 24);
  }

  $scope.filterChanged = function() {
    $scope.filteredList = filterFilter($scope.allItems, {'name': $scope.filter });
    $scope.currentPage = 1;
    $scope.pageChanged();
  }

}]);
