'use strict';

angular.module('dynamo.login', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/login', {
    templateUrl: 'login/login.html',
    controller: 'LoginCtrl'
  });
}])

.controller('LoginCtrl', ['$scope', '$routeParams', 'downloadableService', 'log', '$uibModal', 'filterFilter', function( $scope, $routeParams, downloadableService, log, $uibModal, filterFilter ) {

    $scope.username = '';
    $scope.password = '';

    $scope.login = function() {
      alert($scope.password);
    }

}]);
