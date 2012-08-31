<html><head><title>MySQL Table Viewer</title></head><body>
<?php
$db_host = 'REPLACE_WITH_DB_HOST';
$db_user = 'REPLACE_WITH_DB_USER';
$db_pwd = 'REPLACE_WITH_DB_PASSWORD';

$database = 'REPLACE_WITH_DB_NAME';
$table = 'REPLACE_WITH_TABLE_NAME';

if (!mysql_connect($db_host, $db_user, $db_pwd))
    die("Can't connect to database");

if (!mysql_select_db($database))
    die("Can't select database");

// sending query
$result = mysql_query("SELECT * FROM {$table}");
if (!$result) {
    die("Query to show fields from table failed");
}

$fields_num = mysql_num_fields($result);

echo "<h1>Table: {$table}</h1>";
echo "<h2><table border='1'><tr>";
// printing table headers
for($i=0; $i<$fields_num; $i++)
{
    $field = mysql_fetch_field($result);
    echo "<td>{$field->name}</td>";
}
echo "</tr>\n";
// printing table rows
while($row = mysql_fetch_row($result))
{
    echo "<tr>";

    // $row is array... foreach( .. ) puts every element
    // of $row to $cell variable
    foreach($row as $cell)
        echo "<td>$cell</td>";

    echo "</tr>\n";
}
echo "</table></h2>\n";
mysql_free_result($result);
?>
</body></html>