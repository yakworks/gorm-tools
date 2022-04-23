hazelcast:
  cluster-name: dev
  network:
    port:
      auto-increment: true
      port-count: 100
      port: 5701
    join:
      multicast:
        enabled: true
        multicast-group: 224.2.2.3
        multicast-port: 54327
  map:
    default:
      time-to-live-seconds: 90
    'gorm.tools.security.domain.SecRole':
      time-to-live-seconds: 30
