'use strict';

angular.module('dynamo.music', ['ngRoute', 'ngResource'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/music/:status', {
      templateUrl: 'music/music.html',
      controller: 'MusicCtrl'
    }).when('/music-configuration', {
      templateUrl: 'configuration/configuration-template.html',
      controller: 'MusicConfigCtrl',
      resolve: {
        configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
        }]
      }
    });
  }])

  .controller('MusicCtrl', ['$scope', '$rootScope', '$routeParams', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'BackendService', function ($scope, $rootScope, $routeParams, downloadableService, fileListService, $uibModal, filterFilter, BackendService) {

    $scope.currentPage = 1;
    $scope.allItems = [];
    $scope.filteredList = [];

    $scope.itemsPerPage = 18;

    $scope.imageURL = function (url) {
      return BackendService.getImageURL(url);
    }

    $scope.pageContents = [];
    downloadableService.find('MUSICALBUM', $routeParams.status).then(function (response) {
      $scope.allItems = response.data;
      $scope.pageContents = $scope.allItems.slice(0, $scope.itemsPerPage);
      $scope.filteredList = $scope.allItems.slice(0);
    });

    $scope.want = function (downloadable) {
      downloadableService.want(downloadable.id);
      $scope.removeFromList( downloadable );

      $rootScope.musicAlbumsSuggestionCount = $scope.allItems.length;
      $rootScope.musicAlbumsWantedCount++;
    }

    $scope.removeFromList = function (downloadable) {
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.delete = function (downloadable) {
      downloadableService.delete(downloadable.id);
      $scope.removeFromList(downloadable);
    }

    $scope.updateImage = function (downloadable) {
      downloadableService.updateImage(downloadable.id);
    }

    $scope.redownload = function (downloadable) {
      downloadableService.redownload(downloadable.id);
      downloadable.status = 'WANTED';
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * $scope.itemsPerPage;
      $scope.pageContents = $scope.filteredList.slice(start, start + $scope.itemsPerPage);
    }

    $scope.filterChanged = function () {
      $scope.filteredList = filterFilter($scope.allItems, { 'name': $scope.filter });
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.openFileList = function (downloadable) {

      var modalInstance = $uibModal.open({
        animation: false,
        templateUrl: 'fileList.html',
        controller: 'FileListCtrl',
        size: 'lg',
        resolve: {
          fileList: function () {
            return fileListService.get(downloadable.id);
          }
        }
      });

      modalInstance.result.then(function (selectedItem) {
        $scope.selected = selectedItem;
      });
    };

  }])

  .controller('MusicConfigCtrl', ['$scope', '$rootScope', 'configurationService', 'configuration', function ($scope, $rootScope, configurationService, configuration) {

    $scope.config = configuration.data;

    $scope.itemsToConfigure = [
      $scope.config['MusicManager.folders'],
      $scope.config['MusicManager.cleanDuringImport'],
      $scope.config['MusicManager.musicDownloadProviders'],
      $scope.config['MusicManager.musicQuality'],
      $scope.config['MusicManager.suggesters']
    ];

    $scope.saveSettings = function () {
      configurationService.saveItems($scope.itemsToConfigure);
    }

  }]);
