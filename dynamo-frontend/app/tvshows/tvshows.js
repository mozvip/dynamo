'use strict';

angular.module('dynamo.tvshows', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/tvshows', {
    templateUrl: 'tvshows/tvshows.html',
    controller: 'TVShowsCtrl'
  }).when('/tvshows-add', {
    templateUrl: 'tvshows/tvshows-add.html',
    controller: 'TVShowsCtrl'
  }).when('/tvshow-detail/:tvShowId', {
    templateUrl: 'tvshows/tvshow-detail.html',
    controller: 'TVShowDetailsCtrl',
    resolve: {
      tvshow: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getTVShow( $route.current.params.tvShowId );
      }]
    }
  });
}])

.factory('tvdbService', ['BackendService', function(BackendService){
  var tvdbService = {};
  tvdbService.find = function( title ) {
    return BackendService.get('tvdb/search', {'title' : title, 'language' : 'EN'});
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
  return tvShowsService;
}])

.controller('TVShowDetailsCtrl', ['$scope', 'tvshow', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', function( $scope, tvshow, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal, filterFilter ) {

  $scope.tvshow = tvshow;

}])

.controller('TVShowsCtrl', ['$scope', '$routeParams', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', function( $scope, $routeParams, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal, filterFilter ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

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

  $scope.results = [];
  $scope.searchTVShow = function() {
    tvdbService.find( $scope.title ).then( function( response ) {
      $scope.results = response.data;
    });
  }

  $scope.addTVShow = function( id ) {
    BackendService.post("tvshow/add", {"id": id});
  }

  $scope.openFileList = function ( downloadable) {

    var modalInstance = $uibModal.open({
      animation: false,
      templateUrl: 'fileList.html',
      controller: 'FileListCtrl',
      size: 'lg',
      resolve: {
        fileList: function () {
          return fileListService.get( downloadable.id );
        }
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

}]);
