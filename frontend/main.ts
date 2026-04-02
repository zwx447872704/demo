import { Router } from '@vaadin/router';
import { routes } from './routes';

const outlet = document.querySelector('#outlet')!;
const router = new Router(outlet);
router.setRoutes(routes);