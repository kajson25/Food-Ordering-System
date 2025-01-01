// import { NgModule } from '@angular/core';
import { Routes } from '@angular/router';
import {LoginComponent} from '../login/login.component';
import {AuthGuard} from '../services/auth-guard.service';
import {LoginGuard} from '../services/login-guard.service';

export const routes: Routes = [
  {
    path: '',
    component: LoginComponent,
    canActivate: [LoginGuard],
  },
  {
    path: 'users',
    loadComponent: () =>
      import('../user-list/user-list.component').then((m) => m.UserListComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'edit/:email',
    loadComponent: () =>
      import('../edit-user/edit-user.component').then((m) => m.EditUserComponent),
    canActivate: [AuthGuard],
  },
  {
    path: 'add-user',
    loadComponent: () =>
      import('../add-user/add-user.component').then((m) => m.AddUserComponent),
    canActivate: [AuthGuard],
  },

];
