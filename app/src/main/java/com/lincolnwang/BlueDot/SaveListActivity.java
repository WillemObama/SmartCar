package com.lincolnwang.BlueDot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSON;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SaveListActivity extends AppCompatActivity {

    ListView listView;
    List<String> listFileName;
    SaveListViewAdapter saveListViewAdapter;
    File[] files;
    String path;
    AlertDialog dialogRename;

    public static List<CarAction> listCarAction = new ArrayList<CarAction>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_list);

        listView=(ListView) this.findViewById(R.id.listView);
        listFileName=new ArrayList<String>();
        path = Environment.getExternalStorageDirectory() +"/BlueDot/Save/";
        files = new File(path).listFiles();
        swapDirectory(files);
        for (File file : files) {
            listFileName.add(file.getName());
        }
        saveListViewAdapter=new SaveListViewAdapter(this, listFileName);
        listView.setAdapter(saveListViewAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                // 读文件
                File file=files[position];
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                try {
                    FileInputStream fis =  new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int len=-1;
                    while((len=fis.read(buffer))!=-1){
                        stream.write(buffer, 0, len);
                    }
                    stream.close();
                    fis.close();
                    String fileContent = new String(stream.toByteArray(), "UTF-8");
                    // 处理json
                    List<CarAction> list=new ArrayList<CarAction>(JSON.parseArray(fileContent,CarAction.class));
                    RulePathActivity.listCarAction.clear();
                    RulePathActivity.listCarAction.addAll(list);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                finish();
            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                // TODO Auto-generated method stub
                AlertDialog.Builder builder=new AlertDialog.Builder(SaveListActivity.this);
                builder.setTitle("操作");
                CharSequence[] items = {"删除", "重命名"};
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        switch (which) {
                            case 0: // 删除
                                File file=files[position];
                                if(file.delete()){
                                    Toast.makeText(SaveListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                    listFileName.remove(position);
                                    saveListViewAdapter.notifyDataSetChanged();
                                }else{
                                    Toast.makeText(SaveListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1: // 重命名
                                showRenameDialog(position);
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void swapDirectory(File[] mFiles) {
        if (mFiles != null){
            // 排序
            Arrays.sort(mFiles, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    if (lhs.isDirectory() && !rhs.isDirectory()) {
                        return -1;
                    } else if (!lhs.isDirectory() && rhs.isDirectory()) {
                        return 1;
                    }
                    return compareNames(lhs, rhs);
                }

                private int compareNames(File lhs, File rhs) {
                    return rhs.getName().toLowerCase().compareTo(lhs.getName().toLowerCase());
                }

            });
        }
    }

    private void showRenameDialog(final int position) {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view=this.getLayoutInflater().inflate(R.layout.dialog_rename, null);

        final EditText txtFileName=(EditText) view.findViewById(R.id.txtFileName);
        txtFileName.setText(listFileName.get(position).split("\\.")[0]);

        view.findViewById(R.id.btnSubmit).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                files[position].renameTo(new File(path+txtFileName.getText().toString()+".txt"));
                listFileName.set(position, txtFileName.getText().toString()+".txt");
                saveListViewAdapter.notifyDataSetChanged();
                dialogRename.cancel();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialogRename.cancel();
            }
        });

        builder.setView(view);
        dialogRename=builder.create();
        dialogRename.show();
    }
}
