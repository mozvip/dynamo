'use strict';

angular.module('dynamo.games', ['ngRoute', 'ngResource'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/games/:status', {
      templateUrl: 'games/games.html',
      controller: 'GamesCtrl'
    }).when('/games-configuration', {
      templateUrl: 'games/games-configuration.html',
      controller: 'GamesConfigCtrl',
      resolve: {
        configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
        }]
      }
    });
  }])

  .controller('GamesCtrl', ['$scope', '$routeParams', 'downloadableService', 'fileListService', 'searchResultsService', '$uibModal', 'filterFilter', function ($scope, $routeParams, downloadableService, fileListService, searchResultsService, $uibModal, filterFilter) {

    $scope.currentPage = 1;
    $scope.allItems = [];
    $scope.filteredList = [];

    $scope.image = function (imageURL) {
      return BackendService.getImageURL(imageURL);
    }

    $scope.pageContents = [];
    downloadableService.find('Game', $routeParams.status).then(function (response) {
      $scope.allItems = response.data;
      $scope.pageContents = $scope.allItems.slice(0, 24);
      $scope.filteredList = $scope.allItems.slice(0);
    });

    $scope.want = function (downloadable) {
      downloadableService.want(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.redownload = function (downloadable) {
      downloadableService.redownload(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.openSearchResults = function (downloadable) {
      searchResultsService.openModal(downloadable);
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * 24;
      $scope.pageContents = $scope.filteredList.slice(start, start + 24);
    }

    $scope.filterChanged = function () {
      $scope.filteredList = filterFilter($scope.allItems, { 'name': $scope.filter });
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.openSearchResults = function (downloadable) {
      searchResultsService.openModal(downloadable);
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * 24;
      $scope.pageContents = $scope.filteredList.slice(start, start + 24);
    }

    $scope.filterChanged = function () {
      $scope.filteredList = filterFilter($scope.allItems, { 'name': $scope.filter });
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.openFileList = function (downloadable) {

      var modalInstance = fileListService.openModal(downloadable);
      modalInstance.result.then(function (selectedItem) {
        $scope.selected = selectedItem;
      });
    };

  }])

  .controller('GamesConfigCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

    $scope.config = configuration.data;

    $scope.refreshConfig = function() {

      $scope.itemsToConfigure = [
        $scope.config['GamesManager.platforms'],
        $scope.config['GamesManager.providers']
      ];


    }

    $scope.saveSettings = function () {
      configurationService.saveItems($scope.itemsToConfigure);
    }

    $scope.refreshConfig();

  }]);
