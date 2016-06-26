'use strict';

angular.module('dynamo.log', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/log', {
    templateUrl: 'log/log.html',
    controller: 'LogCtrl'
  });
}])

.controller('LogCtrl', ['$scope', '$routeParams', 'downloadableService', 'languageService', 'fileListService', '$uibModal', 'filterFilter', function( $scope, $routeParams, downloadableService, languageService, fileListService, $uibModal, filterFilter ) {


}]);
