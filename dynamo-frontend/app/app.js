'use strict';

// Declare app level module which depends on views, and components
angular.module('dynamo', [
  'ngRoute',
  'ngResource',
  'ngWebSocket',
  'ui.bootstrap',
  'dynamo.common',
  'dynamo.log',
  'dynamo.home',
  'dynamo.configuration',
  'dynamo.movies',
  'dynamo.music',
  'dynamo.books',
  'dynamo.magazines',
  'dynamo.tvshows'
])

.constant('backendHostAndPort', 'localhost:8081')

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/welcome'});
}])
.controller('MenuCtrl', ['$scope', 'eventDataService', function($scope, eventDataService) {

}]);
