'use strict';

angular.module('dynamo.movies', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/movies/:status', {
    templateUrl: 'movies/movies.html',
    controller: 'MoviesCtrl'
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

  $scope.select = function( selectedMovie ) {
    movieDbSearchService.associate( movie.id, selectedMovie.movieDbId ).then( function( response ) {
      movie = response.data;

      movie.name = selectedMovie.name;
      movie.year = selectedMovie.year;

      $uibModalInstance.dismiss();
    });
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

}])

.controller('MoviesCtrl', ['$scope', '$routeParams', 'downloadableService', 'fileListService', 'searchResultsService', '$uibModal', 'movieDbSearchService', 'filterFilter', function( $scope, $routeParams, downloadableService, fileListService, searchResultsService, $uibModal, movieDbSearchService, filterFilter ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.pageContents = [];
  downloadableService.find( 'MOVIE', $routeParams.status ).then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, 24 );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * 24;
    $scope.pageContents = $scope.filteredList.slice( start, start + 24);
  }

  $scope.movieClicked = function( movie ) {
    fileListService.get( movie.id ).success = function() {
      alert('test');
    }
  }

  $scope.filterChanged = function() {
    $scope.filteredList = filterFilter($scope.allItems, {'name': $scope.filter });
    if ($scope.filterYear) {
      $scope.filteredList = filterFilter($scope.filteredList, {'year': $scope.filterYear });
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
          return movieDbSearchService.find( movie.name, movie.year, 'en' );
        },
        movie: movie
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

}]);
