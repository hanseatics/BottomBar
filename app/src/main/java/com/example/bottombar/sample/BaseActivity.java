/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
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

package com.example.bottombar.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Class description
 *
 * @author ashokvarma
 * @version 1.0
 * @see
 * @since 18 Mar 2016
 */
public class BaseActivity extends AppCompatActivity {


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_activity:
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.action_fragments_activity:
                startActivity(new Intent(this, BottomBarFragmentActivity.class));
                return true;
            case R.id.action_menu_activity:
                startActivity(new Intent(this, MenuInflatedActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}