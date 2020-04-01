#!/bin/bash
echo "Docker Springboot image v1.0"

# Change to core dir
cd /apps/install/core

export EXTERNAL_CONFIG="--spring.config.additional-location=$APP_PROPERTIES_PATH/"

# Copy config file to properties path
if [ ! -z "$PROPERTIES_FILE" ]; then
  echo "Copying property file $PROPERTIES_FILE to target path $APP_PROPERTIES_PATH"
  mkdir -p $APP_PROPERTIES_PATH
  cp $PROPERTIES_FILE $APP_PROPERTIES_PATH/.
fi

# Set timezone if provided
if [ ! -z "$TIMEZONE" ]; then
  echo "Setting timezone to $TIMEZONE"
  ln -snf /usr/share/zoneinfo/$TIMEZONE /etc/localtime && echo $TIMEZONE > /etc/timezone
fi

# Set memory
if [ -z "$HEAP_SIZE" ]; then
  export HEAP_SIZE=1g
  echo "Setting HEAP_SIZE to $HEAP_SIZE"
fi

# Set GC settings
if [ -z "$GC_FLAGS" ]; then
  export GC_FLAGS="-Xmx$HEAP_SIZE -Xms$HEAP_SIZE -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
  echo "Setting GC_FLAGS to $GC_FLAGS"
fi

# Log other settings
if [ ! -z "$JAVA_OPTS" ]; then
  echo "JAVA_OPTS is set to $JAVA_OPTS"
fi

if [ ! -z "$SPRING_OPTS" ]; then
  echo "SPRING_OPTS is set to $SPRING_OPTS"
fi

# Install certs from default location
for filename in $CERTS_PATH/*; do
    cert_name=$(echo "$filename" | sed "s/.*\///")
    echo "Importing certificate $cert_name into Java keystore..."
    $JAVA_HOME/bin/keytool -noprompt \
      -importcert -keystore $JAVA_HOME/lib/security/cacerts \
      -storepass changeit \
      -file "$filename" -alias "$cert_name"
done

# Run cmd
echo "Executing CMD: $@"
exec "$@"
