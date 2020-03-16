# gmail

# gmall-user用户服务 8080

gmall-manage-service用户服务端口service层8071
gmall-manage-web用户服务端口web层8081

# gmall-item-service前台商品详情展示服务端口8072
gmall-item-web前台商品详情展示端口8082

gmall-search-service搜索服务端口 8074
gmall-search-web搜索服务前台端口8083

gmall-cart-service搜索服务端口 8075
gmall-cart-web搜索服务前台端口8084

gmall-passport-web用户认证中心8085
gmall-passport-service用户服务端口8070

gmall-order-web 订单前台端口8086
gmall-order-service订单服务端口8076

gmall-payment端口号8087
问题1：如果redis中的锁已经过期了，然后锁过期的那个请求又执行完毕，回来删锁，删除了其他线程的锁，怎么办？
    答：在redis的键中加入属于自己锁的值，删除前对应一下。
问题2：如果碰巧在查询redis锁还没删除的时候，正在网络传输时锁过期了，怎么办？
    答：lua脚本，在查询时直接删除。