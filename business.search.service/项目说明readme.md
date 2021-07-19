1. 检查证书: GET /_ssl/certificates

2. 在最低安全配置中添加密码保护后，需要配置传输层安全性 (TLS)。传输层处理集群中节点之间的所有内部通信。在集群中设置 SSL/TLS。

- 先决条件:
1. 完成Elastic Stack 的最低安全性中的步骤，以在集群中的每个节点上启用 Elasticsearch 安全功能。然后，您可以使用 TLS 加密节点之间的通信。

2. 您只需为整个集群的内置用户创建一次密码
 
3. 生成证书颁发机构编辑
   您可以在集群中添加任意数量的节点，但它们必须能够相互通信。集群中节点之间的通信由传输模块处理。为了保护您的集群，您必须确保节点间通信经过加密和验证，这是通过双向 TLS 实现的。
   
   在安全集群中，Elasticsearch 节点在与其他节点通信时使用证书来标识自己。
   
   集群必须验证这些证书的真实性。推荐的方法是信任特定的证书颁发机构 (CA)。当节点添加到您的集群时，它们必须使用由同一 CA 签署的证书。
   
   对于传输层，我们建议使用单独的专用 CA，而不是现有的、可能共享的 CA，以便严格控制节点成员资格。使用该elasticsearch-certutil工具为您的集群生成 CA。
   
   1. 使用该elasticsearch-certutil工具为您的集群生成 CA。
     执行命令:
      ./bin/elasticsearch-certutil ca
      
      出现提示时，接受默认文件名，即elastic-stack-ca.p12. 此文件包含 CA 的公共证书和用于为每个节点签署证书的私钥。
      输入您的 CA 的密码。如果您不部署到生产环境，您可以选择将密码留空。
      
   2. 为您的节点生成证书和私钥。您包括elastic-stack-ca.p12在上一步中生成的 输出文件。
      
    ./bin/elasticsearch-certutil cert --ca elastic-stack-ca.p12
      输入您的 CA 的密码，或者如果您没有在上一步中配置密码，请按Enter。
      为证书创建密码并接受默认文件名。
      
      输出文件是一个名为elastic-certificates.p12. 此文件包含节点证书、节点密钥和 CA 证书。
      
      --ca <ca_file>
      用于签署证书的 CA 文件的名称。该elasticsearch-certutil工具的默认文件名是elastic-stack-ca.p12.
      将elastic-certificates.p12文件复制到ES_PATH_CONF 集群中每个节点上的目录。
    
   3. 使用 TLS 加密节点间通信编辑
      传输网络层用于集群中节点之间的内部通信。启用安全功能后，您必须使用 TLS 来确保节点之间的通信是加密的。
      
      现在您已经生成了证书颁发机构和证书，您将更新集群以使用这些文件。
      
      Elasticsearch 监控所有文件，例如证书、密钥、密钥库或信任库，这些文件被配置为与 TLS 相关的节点设置的值。如果您更新任何这些文件，例如当您的主机名更改或您的证书到期时，Elasticsearch 会重新加载它们。以全局 Elasticsearchresource.reload.interval.high设置确定的频率轮询文件的更改 ，默认为 5 秒。 
      
    4. 为集群中的每个节点完成以下步骤。要加入同一个集群，所有节点必须共享相同的cluster.name值。
       
       打开ES_PATH_CONF/elasticsearch.yml文件并进行以下更改：
       
       添加cluster-name设置并输入集群名称：
         cluster.name: my-cluster
       
       添加node.name设置并输入节点的名称。节点名称默认为 Elasticsearch 启动时机器的主机名。
         node.name: node-1
       
       添加以下设置以启用节点间通信并提供对节点证书的访问。
       
       因为您elastic-certificates.p12在集群中的每个节点上都使用相同的文件，所以将验证模式设置为certificate:
       
       xpack.security.transport.ssl.enabled: true
       xpack.security.transport.ssl.verification_mode: certificate 
       xpack.security.transport.ssl.client_authentication: required
       xpack.security.transport.ssl.keystore.path: elastic-certificates.p12
       xpack.security.transport.ssl.truststore.path: elastic-certificates.p12
       
    5. 如果您在创建节点证书时输入了密码，请运行以下命令将密码存储在 Elasticsearch 密钥库中:
     1. /bin/elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password
     2. ./bin/elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
    
    为集群中的每个节点完成前面的步骤。
    重新启动 Elasticsearch。启动和停止Elasticsearch的方法因您的安装方式而异。
    
    例如，如果您使用存档分发版 (tar.gz或.zip)安装 Elasticsearch，则可以Ctrl+C在命令行中输入以停止 Elasticsearch。
    
    您必须执行完整的集群重新启动。配置为使用 TLS 进行传输的节点无法与使用未加密传输连接的节点通信（反之亦然） 
    
  7. SSL/TLS 检查编辑
       如果您启用 Elasticsearch 安全功能，除非您有试用许可证，否则您必须为节点间通信配置 SSL/TLS。
       
       使用环回接口的单节点集群没有此要求。有关更多信息，请参阅 配置安全性: https://www.elastic.co/guide/en/elasticsearch/reference/current/configuring-stack-security.html。
       
       要通过此引导程序检查，您必须 在集群中设置 SSL/TLS。
       
       令牌 SSL 检查编辑
       如果您使用 Elasticsearch 安全功能并且启用了内置令牌服务，则必须将集群配置为对 HTTP 接口使用 SSL/TLS。需要 HTTPS 才能使用令牌服务。
       
       特别是，如果在文件中xpack.security.authc.token.enabled设置为true，则elasticsearch.yml还必须设置 xpack.security.http.ssl.enabled为true。  
    
3. 官方服务端加密配置:
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/security-basic-setup.html#encrypt-internode-communication

  - https://www.elastic.co/guide/en/elasticsearch/reference/7.13/security-settings.html#transport-tls-ssl-settings
  
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/security-basic-setup-https.html
  
  - https://www.elastic.co/cn/blog/configuring-ssl-tls-and-https-to-secure-elasticsearch-kibana-beats-and-logstash
  
  - https://www.elastic.co/cn/blog/configuring-ssl-tls-and-https-to-secure-elasticsearch-kibana-beats-and-logstash#create-ssl
  
  - 安全设置: https://www.elastic.co/guide/en/elasticsearch/reference/current/security-settings.html
  
  - https://opendistro.github.io/for-elasticsearch-docs/old/0.9.0/docs/security/tls-configuration/
  
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/jdk-tls-versions.html
  
  -  https://www.elastic.co/guide/en/elasticsearch/reference/current/security-files.html
  
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/enable-audit-logging.html
  
  - java ssl连接: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_encrypted_communication.html
  
  - 基本认证: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_basic_authentication.html
  
  - 其他认证: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_other_authentication_methods.html
  
  - 认证模式(CA,CERT,CSR,HTTP): https://www.elastic.co/guide/en/elasticsearch/reference/current/certutil.html
  
  - JKS和P12证书转换
  https://www.cnblogs.com/got-my-way/p/6256306.html
  
  - 安全命令: https://www.elastic.co/guide/en/elasticsearch/reference/current/elasticsearch-keystore.html
  
  - 设置密码: https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-passwords.html
  
  - 节点选择器(配合嗅探器使用): https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_node_selector.html
  
  - 嗅探器: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_usage.html
  
  - RestHighLevelClient说明: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
    -  [java 高级 REST 客户端在 Java 低级 REST 客户端之上运行。它的主要目标是公开 API 特定的方法，接受请求对象作为参数并返回响应对象，以便请求编组和响应解组由客户端本身处理。
   
    - 每个 API 都可以同步或异步调用。同步方法返回一个响应对象，而名称以async后缀结尾的异步方法需要一个侦听器参数，一旦收到响应或错误，就会通知（在低级客户端管理的线程池上）。
   
    - Java High Level REST Client 依赖于 Elasticsearch 核心项目。它接受与 相同的请求参数TransportClient并返回相同的响应对象]
