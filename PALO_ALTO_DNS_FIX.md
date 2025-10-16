# Palo Alto DNS Resolution Fix

## Problem

When running the ComLine Edge Service on laptops with Palo Alto GlobalProtect or other corporate security software, you may encounter:

```
io.netty.resolver.dns.DnsResolverTimeoutException: [/8.8.8.8:53, /8.8.4.4:53] query timed out after 5000 milliseconds
```

This happens because corporate security software intercepts and modifies DNS traffic, causing Netty's native DNS resolver to timeout.

## Quick Fix (Recommended)

Enable the JVM DNS resolver which works better with corporate security software:

```bash
# Option 1: Environment variable
export WEBCLIENT_USE_JVM_DNS_RESOLVER=true
mvn spring-boot:run

# Option 2: Command line argument
mvn spring-boot:run -Dspring-boot.run.arguments="--webclient.use-jvm-dns-resolver=true"

# Option 3: Java JAR
java -jar comline-edge.jar --webclient.use-jvm-dns-resolver=true

# Option 4: Docker
docker run -p 8080:8080 \
  -e WEBCLIENT_USE_JVM_DNS_RESOLVER=true \
  -e COMLINE_CUSTOMER_NUMBER=your-number \
  -e COMLINE_PASSWORD=your-password \
  ghcr.io/conrad-ccp/comline-edge:latest
```

## How It Works

The fix configures Netty's HTTP client to use Java's built-in DNS resolver (`DefaultAddressResolverGroup.INSTANCE`) instead of Netty's native DNS resolver. This bypasses the DNS interception issues caused by corporate security software.

**Code Changes:**
```java
// In WebClientConfig.java
if (useJvmDnsResolver) {
    httpClient = httpClient.resolver(DefaultAddressResolverGroup.INSTANCE);
}
```

## Additional Solutions

### Increase DNS Timeout

If switching to JVM DNS resolver doesn't fully resolve the issue:

```bash
export WEBCLIENT_CONNECTION_TIMEOUT=30000  # 30 seconds
export WEBCLIENT_READ_TIMEOUT=60000        # 60 seconds
```

### Configure JVM DNS Cache

Add these JVM arguments to improve DNS caching:

```bash
java -jar comline-edge.jar \
  -Dnetworkaddress.cache.ttl=60 \
  -Dnetworkaddress.cache.negative.ttl=10 \
  -Djava.net.preferIPv4Stack=true \
  --webclient.use-jvm-dns-resolver=true
```

### Use IP Address

If DNS resolution completely fails, use the IP address directly:

```bash
# First resolve the IP
nslookup ctofinder.comline-shop.de

# Then use it
export COMLINE_BASE_URL=https://IP_ADDRESS/4DCGI/direct
```

### Add to hosts file

Add the hostname to your system's hosts file:

**Linux/macOS:**
```bash
sudo vi /etc/hosts
# Add line:
# 123.45.67.89  ctofinder.comline-shop.de
```

**Windows:**
```cmd
notepad C:\Windows\System32\drivers\etc\hosts
# Add line:
# 123.45.67.89  ctofinder.comline-shop.de
```

## Verification

After applying the fix, you should see this log message on startup:

```
INFO ... d.c.ccp.comline.config.WebClientConfig : Using JVM DNS resolver (workaround for corporate security software like Palo Alto)
```

Instead of:

```
INFO ... d.c.ccp.comline.config.WebClientConfig : Using Netty's default DNS resolver
```

## Complete Configuration Reference

All available WebClient configuration options:

```yaml
webclient:
  connection:
    timeout: 10000              # Connection timeout in ms (default: 10s)
  read:
    timeout: 30000              # Read timeout in ms (default: 30s)
  write:
    timeout: 10000              # Write timeout in ms (default: 10s)
  max:
    connections: 100            # Max connections in pool (default: 100)
  pending:
    acquire:
      timeout: 45000            # Pending acquire timeout in ms (default: 45s)
  use-jvm-dns-resolver: true    # Use JVM DNS resolver (default: false)
```

Or via environment variables:

```bash
export WEBCLIENT_CONNECTION_TIMEOUT=10000
export WEBCLIENT_READ_TIMEOUT=30000
export WEBCLIENT_WRITE_TIMEOUT=10000
export WEBCLIENT_MAX_CONNECTIONS=100
export WEBCLIENT_PENDING_ACQUIRE_TIMEOUT=45000
export WEBCLIENT_USE_JVM_DNS_RESOLVER=true
```

## Corporate Network Checklist

If you're still experiencing issues after enabling JVM DNS resolver:

- [ ] Check if VPN (GlobalProtect, Cisco AnyConnect, etc.) is connected
- [ ] Verify proxy settings are not blocking DNS
- [ ] Test DNS resolution: `nslookup ctofinder.comline-shop.de`
- [ ] Check if firewall allows outbound HTTPS (port 443)
- [ ] Try from a different network (mobile hotspot) to isolate the issue
- [ ] Contact IT support if the hostname is completely blocked
- [ ] Consider requesting an exception for the ComLine API domain

## Related Issues

This fix also helps with:
- Zscaler Internet Security
- Cisco Umbrella
- McAfee Web Gateway
- Symantec Web Security Service
- Other SSL-inspecting proxies and security software

## Technical Details

**Why Netty's DNS Resolver Fails:**
- Corporate security software intercepts DNS queries at the network level
- SSL inspection may interfere with DNS-over-HTTPS
- Firewall rules may block direct DNS queries to public resolvers (8.8.8.8, etc.)
- Netty's native resolver expects direct, unmodified DNS responses

**Why JVM DNS Resolver Works:**
- Uses Java's standard networking stack
- Respects system DNS configuration
- Works through corporate DNS servers
- Compatible with DNS interception/inspection
- Falls back to system resolver configuration

## Support

If you continue to experience DNS issues:

1. Enable debug logging:
   ```yaml
   logging:
     level:
       de.conrad.ccp.comline: DEBUG
       reactor.netty: DEBUG
       io.netty.resolver.dns: TRACE
   ```

2. Collect logs and contact support with:
   - Full error stack trace
   - Network configuration (VPN, proxy, DNS servers)
   - Security software version (Palo Alto GlobalProtect, etc.)
   - Output of `nslookup ctofinder.comline-shop.de`
