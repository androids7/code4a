package cn.atd3.code4a.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.atd3.code4a.Constant;
import cn.atd3.code4a.model.model.ArticleModel;
import cn.atd3.code4a.net.Remote;
import cn.atd3.code4a.view.inter.ArticleViewInterface;

import static cn.atd3.code4a.Constant.ERROR;

/**
 * Created by harry on 2018/1/15.
 */

public class ViewArticlePresenter {

    ArticleViewInterface avi;
    URLImageParser urlImageParser;

    int articleid=-1;
    int userid=-1;

    public ViewArticlePresenter(ArticleViewInterface avi)
    {
        this.avi=avi;
    }

    public void initImageGetter(TextView tv)
    {

         urlImageParser =new URLImageParser(tv);


    }

    public void shouWaitDialog()
    {
        avi.showWaitDialog();
    }
    public void dismissWaitDialog()
    {
        avi.dismissWaitDialog();
    }

    public void checkArticle(int articleid,int userid)
    {
        if (articleid== -1||userid==-1) {
           avi.showToast(ERROR,"error");
           avi.dismissWaitDialog();//错误，取消弹窗
           return;
        }
        this. articleid=articleid;
        this. userid=userid;
    }

    public void loadArticle()
    {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String usern="";
                        try {
                            Object username= Remote.user.method("id2name",String.class).call(userid);
                            if(username instanceof String)
                            {
                                usern=(String)username;
                            }
                              else
                            {
                                usern="error";
                            }

                            Log.e("username",""+username);
                        }
                        catch (Exception e)
                        {
                            Log.e("userid",e.toString());
                        }

                        avi.loadUser(usern);//UI加载用户名


                        String text="";
                        try {
                            Object a = Remote.article.method("getArticleById", ArticleModel.class).call(articleid);
                            if(a instanceof ArticleModel)
                            {
                                Log.i("obj","is article");
                                if(((ArticleModel) a).getContent()!=null)
                                {
                                    // fix: kotlin keywords abstract error
                                    text= ((ArticleModel) a).getContent();   // abstract 属于关键字，不能用作属性名直接获取
                                    Set<String> imgSet= getImgStr(text);
                                    for(String imgurl : imgSet)
                                    {
                                        Log.e("img",""+imgurl);
                                        text=text.replace(imgurl, Constant.serverAddress+imgurl);//地址转换成绝对地址
                                    }
                                    Log.e("final",text);
                                }
                                else
                                {
                                    Log.e("obj","null");
                                    text="";
                                }
                            }

                            avi.loadArticle(text,urlImageParser);//显示文章

                        }
                        catch (Exception e)
                        {
                            Log.e("net error",""+e);
                        }


                    }
                }

        ).start();

    }

    /**
     * 得到网页中图片的地址
     * @param
     */
    private Set<String> getImgStr(String htmlStr)  {
        HashSet pics = new HashSet<String>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
         String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                if(!m.group(1).startsWith("http:"))//图片为表情就跳过
                    pics.add(m.group(1));
            }
        }
        return pics;
    }

     class URLImageParser implements Html.ImageGetter {
        private TextView mTextView;
        public URLImageParser(TextView mTextView ){
                this.mTextView=mTextView;
        }


         @Override
         public Drawable getDrawable(String s) {
             final URLDrawable urlDrawable = new URLDrawable();
             ImageLoader.getInstance().loadImage(s,
                 new SimpleImageLoadingListener(){
                     @Override
                     public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                         urlDrawable.drawBitmap = loadedImage;
                         urlDrawable.setBounds(0, 0, loadedImage.getWidth(), loadedImage.getHeight());
                         mTextView.invalidate();
                         mTextView.setText( mTextView.getText());
                     }
                 });
             return urlDrawable;
         }
     }


     class URLDrawable extends BitmapDrawable {
         Bitmap drawBitmap= null;
         @Override
         public void draw(Canvas canvas) {
             if (drawBitmap != null) {
              canvas.drawBitmap(drawBitmap, 0f, 0f, getPaint());
          }
         }

    }

}