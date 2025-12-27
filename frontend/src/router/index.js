import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Dashboard from '../views/Dashboard.vue'
import Query from '../views/Query.vue'
import Analysis from '../views/Analysis.vue'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        meta: { title: '首页' }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '数据大屏' }
    },
    {
        path: '/query',
        name: 'Query',
        component: Query,
        meta: { title: '交互式查询' }
    },
    {
        path: '/analysis',
        name: 'Analysis',
        component: Analysis,
        meta: { title: '离线分析' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title} - ETC大数据管理平台`
    next()
})

export default router
