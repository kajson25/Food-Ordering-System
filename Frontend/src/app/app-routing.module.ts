import { Routes } from '@angular/router';
import { LoginComponent } from '../login/login.component';
import { AuthGuard } from '../services/auth-guard.service';
import { LoginGuard } from '../services/login-guard.service';

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
    data: { requiredPermission: 'CAN_READ_USER' },
  },
  {
    path: 'edit/:email',
    loadComponent: () =>
      import('../edit-user/edit-user.component').then((m) => m.EditUserComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_UPDATE_USER' },
  },
  {
    path: 'add-user',
    loadComponent: () =>
      import('../add-user/add-user.component').then((m) => m.AddUserComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_CREATE_USER' },
  },
  {
    path: 'search-orders',
    loadComponent: () =>
      import('../search-orders/search-orders.component').then((m) => m.SearchOrdersComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_SEARCH_ORDER' },
  },
  {
    path: 'create-order',
    loadComponent: () =>
      import('../create-order/create-order.component').then((m) => m.CreateOrderComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_CREATE_ORDER' },
  },
  {
    path: 'track-order',
    loadComponent: () =>
      import('../track-order/track-order.component').then((m) => m.TrackOrderComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_TRACK_ORDER' },
  },
  {
    path: 'show-errors',
    loadComponent: () =>
      import('../error-history/error-history.component').then((m) => m.ErrorHistoryComponent),
    canActivate: [AuthGuard],
    data: { requiredPermission: 'CAN_VIEW_ERRORS' },
  },
];
