# Angular web application 
<img src="https://img.shields.io/github/package-json/v/beatum/ng-web" alt="Version"> <img src="https://img.shields.io/github/checks-status/beatum/ng-web/master"><img src="https://img.shields.io/github/license/beatum/ng-web"><img alt="GitHub all releases" src="https://img.shields.io/github/downloads/beatum/ng-web/total">


This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 15.0.3.

## Overviews：

![Login](https://github.com/beatum/ng-web/blob/master/imgs/login.jpg)

![Main](https://github.com/beatum/ng-web/blob/master/imgs/main.jpg)

## Routing:

```typescript
const routes: Routes = [
  { path: '', redirectTo: '\login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'main', component: MainComponent, canActivate: [AccessPrivilegeGuardGuard],
    children: [
      { path: 'appuser', component: AppuserComponent, canActivate: [AccessPrivilegeGuardGuard], data: { uid: "appuser", name: "User", useCache: true } },
      { path: 'appmenu', component: AppmenuComponent, canActivate: [AccessPrivilegeGuardGuard], data: { uid: "appmenu", name: "Menu", useCache: true } },
      { path: 'appusergroup', component: AppusergroupComponent, canActivate: [AccessPrivilegeGuardGuard], data: { uid: "appusergroup", name: "User group", useCache: true } }
    ]
  }
];
```

## Development server
Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Acknowledgment: 
[Clarity Design ](https://github.com/vmware-clarity/ng-clarity)

​			
