'use strict';

angular.module('dynamo.magazines', ['ngRoute', 'ngResource'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/magazines/:status', {
      templateUrl: 'magazines/magazines.html',
      controller: 'MagazinesCtrl',
      resolve: {
        languages: ['languageService', function (languageService) {
          return languageService.find();
        }]
      }
    }).when('/magazines-configuration', {
      templateUrl: 'configuration/configuration-template.html',
      controller: 'MagazinesConfigCtrl',
      resolve: {
        configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
        }]
      }
    });
  }])

  .controller('MagazinesConfigCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

    $scope.config = configuration.data;

    $scope.itemsToConfigure = [
      $scope.config['MagazineManager.folders'],
      $scope.config['MagazineManager.defaultLanguage'],
      $scope.config['RefreshKioskExecutor.suggesters'],
      $scope.config['MagazineManager.providers']
    ];

    $scope.saveSettings = function () {
      configurationService.saveItems($scope.itemsToConfigure);
    }

  }])

  .controller('MagazinesCtrl', ['$scope', '$rootScope', '$routeParams', 'downloadableService', 'languages', 'fileListService', 'searchResultsService', '$filter', '$uibModal', 'filterFilter', 'BackendService', function ($scope, $rootScope, $routeParams, downloadableService, languages, fileListService, searchResultsService, $filter, $uibModal, filterFilter, BackendService) {

    $scope.currentPage = 1;
    $scope.allItems = [];
    $scope.filteredList = [];
    $scope.itemsPerPage = 24;

    $scope.imageURL = function (url) {
      return BackendService.getImageURL(url);
    }

    $scope.languages = languages.data;

    $scope.pageContents = [];
    downloadableService.find('MAGAZINEISSUE', $routeParams.status).then(function (response) {
      $scope.allItems = $filter('orderBy')(response.data, '-creationDate');
      $scope.pageContents = $scope.allItems.slice(0, $scope.itemsPerPage);
      $scope.filteredList = $scope.allItems.slice(0);
    });

    $scope.want = function (downloadable) {
      downloadableService.want(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();

      $rootScope.magazinesSuggestionCount = $scope.allItems.length;
      $rootScope.magazinesWantedCount++;
    }

    $scope.redownload = function (downloadable) {
      downloadableService.redownload(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.delete = function (downloadable) {
      downloadableService.delete(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * $scope.itemsPerPage;
      $scope.pageContents = $scope.filteredList.slice(start, start + $scope.itemsPerPage);
    }

    $scope.filterChanged = function () {

      var filterObject = { 'name': $scope.filter };
      if ($scope.filterLanguage) {
        filterObject.language = $scope.filterLanguage;
      }
      if ($scope.filterYear) {
        filterObject.year = $scope.filterYear;
      }

      $scope.filteredList = filterFilter($scope.allItems, filterObject);
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.openSearchResults = function (downloadable) {
      searchResultsService.openModal(downloadable);
    }

  $scope.openFileList = function ( downloadable) {
    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

  }]);
