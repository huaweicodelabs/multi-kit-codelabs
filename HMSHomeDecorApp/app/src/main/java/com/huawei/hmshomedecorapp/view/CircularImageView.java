/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hmshomedecorapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class CircularImageView extends androidx.appcompat.widget.AppCompatImageView {

    public CircularImageView( Context context ) {
        super( context );
    }

    public CircularImageView( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    public CircularImageView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
    }

    @Override
    protected void onDraw( @NonNull Canvas canvas ) {

        Drawable drawable = getDrawable( );

        if ( drawable == null ) {
            return;
        }

        if ( getWidth( ) == 0 || getHeight( ) == 0 ) {
            return;
        }
        Bitmap b = ( (BitmapDrawable) drawable ).getBitmap( );
        Bitmap bitmap = b.copy( Bitmap.Config.ARGB_8888, true );

        int w = getWidth( ); // h = getHeight( )

        Bitmap roundBitmap = getCroppedBitmap( bitmap, w );
        canvas.drawBitmap( roundBitmap, 0, 0, null );

    }

    private static Bitmap getCroppedBitmap( @NonNull Bitmap bmp, int radius ) {
        Bitmap bitmap;

        if ( bmp.getWidth( ) != radius || bmp.getHeight( ) != radius ) {
            float smallest = Math.min( bmp.getWidth( ), bmp.getHeight( ) );
            float factor = smallest / radius;
            bitmap = Bitmap.createScaledBitmap( bmp, ( int ) ( bmp.getWidth( ) / factor ), ( int ) ( bmp.getHeight( ) / factor ), false );
        }
        else {
            bitmap = bmp;
        }

        Bitmap output = Bitmap.createBitmap( radius, radius,
                Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( output );

        final Paint paint = new Paint( );
        final Rect rect = new Rect( 0, 0, radius, radius );

        paint.setAntiAlias( true );
        paint.setFilterBitmap( true );
        paint.setDither( true );
        canvas.drawARGB( 0, 0, 0, 0 );
        paint.setColor( Color.parseColor( "#BAB399" ) );
        canvas.drawCircle( (radius / (2 + 0.7f)),
                (radius / (2 + 0.7f)), radius / (2 + 0.1f), paint );
        paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_IN ) );
        canvas.drawBitmap( bitmap, rect, rect, paint );

        return output;
    }

}
