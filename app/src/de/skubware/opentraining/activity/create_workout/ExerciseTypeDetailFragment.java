package de.skubware.opentraining.activity.create_workout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.skubware.opentraining.R;
import de.skubware.opentraining.basic.ExerciseType;
import de.skubware.opentraining.basic.FitnessExercise;
import de.skubware.opentraining.basic.Workout;
import de.skubware.opentraining.db.DataHelper;

/**
 * A fragment representing a single ExerciseType detail screen. This fragment is
 * either contained in a {@link ExerciseTypeListActivity} in two-pane mode (on
 * tablets) or a {@link ExerciseTypeDetailActivity} on handsets.
 */
public class ExerciseTypeDetailFragment extends SherlockFragment {
	/** Tag for logging */
	public static final String TAG = ExerciseTypeDetailFragment.class.getName();

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_EXERCISE = "exercise";

	public static final String ARG_WORKOUT = "workout";

	/**
	 * The {@link ExerciseType} this fragment is presenting.
	 */
	private ExerciseType mExercise;
	private Workout mWorkout;

	private GestureDetector mGestureScanner;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ExerciseTypeDetailFragment() {
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of changes.
	 */
	public interface Callbacks {
		/**
		 * Callback for when the Workout has changed.
		 */
		public void onWorkoutChanged(Workout w);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setHasOptionsMenu(true);

		mExercise = (ExerciseType) getArguments().getSerializable(ExerciseTypeDetailFragment.ARG_EXERCISE);
		mWorkout = (Workout) getArguments().getSerializable(ExerciseTypeDetailFragment.ARG_WORKOUT);

		this.getActivity().setTitle(mExercise.getLocalizedName());
	}

	
	/** Saves the state of this Fragment, e.g. when screen orientation changed. */
	@Override
	public void onSaveInstanceState (Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putSerializable(ExerciseTypeDetailFragment.ARG_EXERCISE, mExercise);
		outState.putSerializable(ExerciseTypeDetailFragment.ARG_WORKOUT, mWorkout);
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_exercisetype_detail, container, false);

		// show the current exercise

		ImageView imageview = (ImageView) rootView.findViewById(R.id.imageview);

		// set gesture detector
		this.mGestureScanner = new GestureDetector(this.getActivity(), new ExerciseDetailOnGestureListener(this, imageview, mExercise));

		// Images
		if (!mExercise.getImagePaths().isEmpty()) {
			DataHelper data = new DataHelper(getActivity());
			imageview.setImageDrawable(data.getDrawable(mExercise.getImagePaths().get(0).toString()));
		} else {
			imageview.setImageResource(R.drawable.ic_launcher);
		}

		// Image license
		TextView image_license = (TextView) rootView.findViewById(R.id.textview_image_license);
		if (mExercise.getImageLicenseMap().values().iterator().hasNext()) {
			image_license.setText(mExercise.getImageLicenseMap().values().iterator().next());
		} else {
			image_license.setText("Keine Lizenzinformationen vorhanden");
		}

		rootView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureScanner.onTouchEvent(event);
			}
		});

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		// MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.exercise_detail_menu, menu);

		// configure menu_item_add_exercise
		MenuItem menu_item_add_exercise = (MenuItem) menu.findItem(R.id.menu_item_add_exercise);
		menu_item_add_exercise.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {

				// assert, that an exercise was choosen
				if (mExercise == null) {
					Log.wtf(TAG, "No exercise has been choosen. This should not happen");
					return true;
				}

				// add exercise to workout or create a new one
				if (mWorkout == null) {
					mWorkout = new Workout("My Plan", new FitnessExercise(mExercise));
				} else {
					mWorkout.addFitnessExercise(new FitnessExercise(mExercise));
				}

				// update Workout in Activity
				if (getActivity() instanceof Callbacks) {
					// was launched by ExerciseTypeListActivity
					((Callbacks) getActivity()).onWorkoutChanged(mWorkout);
				} else {
					// was launched by ExerciseTypeDetailActivity
					Intent i = new Intent();
					i.putExtra(ExerciseTypeListActivity.ARG_WORKOUT, mWorkout);
					getActivity().setResult(Activity.RESULT_OK, i);
					getActivity().finish();
				}
				
				Toast.makeText(getActivity(),
						getString(R.string.exercise) + " " + mExercise.getLocalizedName() + " " + getString(R.string.has_been_added),
						Toast.LENGTH_SHORT).show();

				return true;
			}
		});


	}


}
