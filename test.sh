classpath="vd-tool.jar"

for file in "C:/Program Files/Android/Android Studio/plugins/"*; do
  if [ -d "$file/lib" ]; then
    classpath="$classpath;$file/lib/*"
  fi
done

java_bin="C:\\Program Files\\Android\\Android Studio\\jbr\\bin"

"$java_bin\\java" -Djava.awt.headless=true -classpath "$classpath" com.android.ide.common.vectordrawable.VdCommandLineTool -c 
