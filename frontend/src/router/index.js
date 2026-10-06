import { createRouter, createWebHashHistory } from 'vue-router'

// 注意：main.js 里没有 app.use(router)，路由实例由 App.vue 直接驱动，
// 所以这里全部用静态 import —— <component :is> 拿到的是组件对象本身。
import EscortTaskView from '../views/EscortTaskView.vue'
import CashVehicleView from '../views/CashVehicleView.vue'
import EscortMemberView from '../views/EscortMemberView.vue'
import RouteView from '../views/RouteView.vue'

const routes = [
  { path: '/', redirect: '/tasks' },
  { path: '/tasks', name: 'tasks', component: EscortTaskView },
  { path: '/vehicles', name: 'vehicles', component: CashVehicleView },
  { path: '/members', name: 'members', component: EscortMemberView },
  { path: '/routes', name: 'routes', component: RouteView },
  { path: '/:pathMatch(.*)*', redirect: '/tasks' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
