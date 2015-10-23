// import { WebDevTecService, ITecThing } from '../components/webDevTec/webDevTec.service';

export interface IUser {
  id: string;
}

export class UsersController {
  public currentUser: IUser;
  public userList: IUser[];
  /* @ngInject */
  constructor () {;
    this.userList = [{ id: 'test' }, { id: 'sample' }, { id: 'third' }];
    this.currentUser = this.userList[0];
  }

}
