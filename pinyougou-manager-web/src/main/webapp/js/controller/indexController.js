app.controller("indexController",function ($scope,$controller,loginService) {
    //读取当前登录人
    $scope.showLoginName=function () {
        loginService.loginName().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        )
    }
});