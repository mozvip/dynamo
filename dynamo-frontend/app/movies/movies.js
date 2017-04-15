'use strict';

angular.module('dynamo.movies', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/movies/:status', {
    templateUrl: 'movies/movies.html',
    controller: 'MoviesCtrl'
  }).when('/movies-configuration', {
    templateUrl: 'configuration/configuration-template.html',
    controller: 'MoviesConfigCtrl',
    resolve: {
      configuration: ['configurationService', function(  configurationService  ) {
        return configurationService.getItems();
      }]
    }    
  }).when('/movies-manual-add', {
    templateUrl: 'movies/movies-manual-add.html',
    controller: 'MoviesManualAddCtrl',
    resolve: {
      configuration: ['configurationService', function(  configurationService  ) {
        return configurationService.getItems();
      }],
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }]      
    }    
  });
}])

.factory('movieDbSearchService', ['BackendService', '$http', function(BackendService, $http){
  var movieDbSearchService = {};
  movieDbSearchService.find =  function( name, year, lang ) {
    var url = 'movie-db?name=' + name + '&lang=' + lang;
    if (year) {
      url = url + "&year=" + year;
    }
    return BackendService.get( url );
  }
  movieDbSearchService.associate =  function( id, movieDbId ) {
    return $http.put(BackendService.getBackendURL() + 'movie-db?id=' + id + '&movieDbId=' + movieDbId);
  }
  return movieDbSearchService;
}])

. controller('MoviesManualAddCtrl', ['$scope', 'movieDbSearchService', 'filterFilter', 'BackendService', 'languages', function($scope, movieDbSearchService, filterFilter, BackendService, languages) {

  $scope.languages = languages.data;

  $scope.name = '';
  $scope.year = '';
  $scope.language = 'EN';
  $scope.subtitlesLanguage = '';

  $scope.search = function() {
    movieDbSearchService.find( $scope.name, $scope.year, $scope.language ).then( function( response ) {
      $scope.results = response.data;
    });
  }

  $scope.selectMovie = function( movie ) {
    BackendService.post('movies/add', {
      'movieDbId': movie.id,
      'audioLanguage': movie.audiolanguage,
      'subtitlesLanguage': movie.subtitlesLanguage
    }).then(function() {
      $scope.results = filterFilter($scope.results, {'id': '!' + movie.id });
    });
  }


}])

. controller('MovieSearchCtrl', ['$scope', '$uibModalInstance', 'fileList', 'movies', 'movie', 'movieDbSearchService', function($scope, $uibModalInstance, fileList, movies, movie, movieDbSearchService) {

  $scope.files = fileList.data;
  $scope.movies = movies.data;
  $scope.movieName = movie.name;
  $scope.movieYear = ( movie.year != -1 ? movie.year : '');

  $scope.search = function() {
    var year = ($scope.movieYear ? $scope.movieYear : -1);
    movieDbSearchService.find( $scope.movieName, year, 'en').then( function( response ) {
      $scope.movies = response.data;
    });
  };

  $scope.select = function( movieDbId ) {
    movieDbSearchService.associate( movie.id, movieDbId ).then( function( response ) {
      movie = response.data;
      $uibModalInstance.close( movie );
    });
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

}])

.controller('MoviesConfigCtrl', ['$scope', '$rootScope', 'configurationService', 'configuration', function( $scope, $rootScope, configurationService, configuration ) {

  $scope.config = configuration.data;

  $scope.itemsToConfigure = [
    $scope.config['MovieManager.folders'],
    $scope.config['RefreshMovieSuggestionExecutor.movieSuggesters'],
    $scope.config['MovieManager.movieDownloadProviders'],
    $scope.config['MovieManager.defaultQuality'],
    $scope.config['MovieManager.minimumSuggestionRating'],
    $scope.config['MovieManager.metaDataLanguage'],
    $scope.config['MovieManager.audioLanguage'],
    $scope.config['MovieManager.subtitlesLanguage'],
    $scope.config['MovieManager.minimumSizeFor720'],
    $scope.config['MovieManager.maximumSizeFor720'],
    $scope.config['MovieManager.minimumSizeFor1080'],
    $scope.config['MovieManager.maximumSizeFor1080'],
    $scope.config['MovieManager.wordsBlackList'],
    $scope.config['MovieCleanupTask.rename']    
  ];

    $scope.saveSettings = function () {
      configurationService.saveItems( $scope.itemsToConfigure );
    }

}])

.controller('MoviesCtrl', ['$scope', '$rootScope', '$routeParams', 'downloadableService', 'fileListService', 'searchResultsService', '$uibModal', 'movieDbSearchService', 'filterFilter', 'BackendService', function( $scope, $rootScope, $routeParams, downloadableService, fileListService, searchResultsService, $uibModal, movieDbSearchService, filterFilter, BackendService ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.itemsPerPage = 18;

  $scope.imageURL = function( url ) {
    return BackendService.getImageURL( url );
  }    

  $scope.pageContents = [];
  downloadableService.find( 'MOVIE', $routeParams.status ).then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, $scope.itemsPerPage );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * $scope.itemsPerPage;
    $scope.pageContents = $scope.filteredList.slice( start, start + $scope.itemsPerPage);
  }

  $scope.removeFromList = function( downloadable ) {
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.want = function( downloadable ) {
    downloadableService.want( downloadable.id );
    $scope.removeFromList( downloadable );

    $rootScope.moviesSuggestionCount = $scope.allItems.length;
    $rootScope.moviesWantedCount ++;    
  }

  $scope.updateImage = function( downloadable ) {
    downloadableService.updateImage( downloadable.id );
  }

  $scope.delete = function( downloadable ) {
    downloadableService.delete( downloadable.id );
    $scope.removeFromList( downloadable );
  }

  $scope.redownload = function( downloadable ) {
    downloadableService.redownload( downloadable.id );
    downloadable.status = 'WANTED';
  }

  $scope.filterChanged = function() {
    var filter = {'name': $scope.filter};
    if ($scope.filterYear) {
      filter['year'] = $scope.filterYear;
    }
    $scope.filteredList = filterFilter($scope.allItems, filter);
    if ($scope.filterRating) {
      $scope.filteredList = $scope.filteredList.filter( function( element ) {
        return element.rating >= $scope.filterRating;
      });
    }
    $scope.currentPage = 1;
    $scope.pageChanged();
  }

  $scope.openSearchResults = function( downloadable ) {
    searchResultsService.openModal( downloadable );
  }

  $scope.openFileList = function ( downloadable) {
    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

  $scope.openMovieSearch = function ( movie) {

    var movieName = movie.name;
    if (movieName.endsWith(".mkv")) {
      movieName = movieName.substr(0, movieName.length - 4);
    }

    var modalInstance = $uibModal.open({
      animation: false,
      templateUrl: 'movieSearch.html',
      controller: 'MovieSearchCtrl',
      size: 'lg',
      resolve: {
        fileList: function () {
          return fileListService.get( movie.id );
        },
        movies: function () {
          return movieDbSearchService.find( movieName, movie.year, 'en' );
        },
        movie: movie
      }
    });

    modalInstance.result.then(function (selectedItem) {
      movie.name = selectedItem.name;
      movie.year = selectedItem.year;
      movie.image = selectedItem.image;
    });
  };

}]);
