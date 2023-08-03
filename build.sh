classpath="."

for file in "C:/Program Files/Android/Android Studio/plugins/"*; do
  if [ -d "$file/lib" ]; then
    classpath="$classpath;$file/lib/*"
  fi
done
classpath="$classpath;./com/android/ide/common/vectordrawable/*.java"

java_bin="C:\\Program Files\\Android\\Android Studio\\jbr\\bin"

"$java_bin\\javac" -verbose -classpath  "$classpath" -d . -s . "src\\com\\android\\ide\\common\\vectordrawable\\VdCommandLineTool.java" 

"$java_bin\\jar" cvfe vd-tool.jar VdCommandLineTool com\\android\\ide\\common\\vectordrawable\\*.class
