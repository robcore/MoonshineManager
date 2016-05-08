/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.fragments.BaseControlFragment;
import com.grarak.kerneladiutor.fragments.BaseFragment;
import com.grarak.kerneladiutor.fragments.DescriptionFragment;
import com.grarak.kerneladiutor.utils.ViewUtils;
import com.grarak.kerneladiutor.utils.kernel.cpu.CPUBoost;
import com.grarak.kerneladiutor.utils.kernel.cpu.CPUFreq;
import com.grarak.kerneladiutor.utils.kernel.cpu.Misc;
import com.grarak.kerneladiutor.views.recyclerview.CardView;
import com.grarak.kerneladiutor.views.recyclerview.DescriptionView;
import com.grarak.kerneladiutor.views.recyclerview.RecyclerViewItem;
import com.grarak.kerneladiutor.views.recyclerview.SeekBarView;
import com.grarak.kerneladiutor.views.recyclerview.SelectView;
import com.grarak.kerneladiutor.views.recyclerview.SwitchView;
import com.grarak.kerneladiutor.views.recyclerview.TitleView;
import com.grarak.kerneladiutordonate.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 01.05.16.
 */
public class CPUFragment extends BaseControlFragment {

    private SelectView mCPUMaxBig;
    private SelectView mCPUMinBig;
    private SelectView mCPUMaxScreenOffBig;
    private SelectView mCPUGovernorBig;
    private SelectView mCPUMaxLITTLE;
    private SelectView mCPUMinLITTLE;
    private SelectView mCPUMaxScreenOffLITTLE;
    private SelectView mCPUGovernorLITTLE;

    private int mCPUMaxFreqBig;
    private int mCPUMinFreqBig;
    private int mCPUMaxScreenOffFreqBig;
    private String mCPUGovernorStrBig;
    private int mCPUMaxFreqLITTLE;
    private int mCPUMinFreqLITTLE;
    private int mCPUMaxScreenOffFreqLITTLE;
    private String mCPUGovernorStrLITTLE;

    private GovernorTunableFragment mGovernorTunableFragment;
    private AlertDialog.Builder mGovernorTunableErrorDialog;

    private Thread mRefreshThread;

    @Override
    protected BaseFragment getForegroundFragment() {
        return mGovernorTunableFragment = new GovernorTunableFragment();
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(ApplyOnBootFragment.newInstance(ApplyOnBootFragment.CPU));
        if (CPUFreq.getCpuCount() > 1) {
            addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.cores,
                    CPUFreq.getCpuCount()), null));
        }

        if (mGovernorTunableErrorDialog != null) {
            mGovernorTunableErrorDialog.show();
        }
    }

    @Override
    protected List<RecyclerViewItem> addItems(List<RecyclerViewItem> items) {
        freqInit(items);
        if (Misc.hasMcPowerSaving()) {
            mcPowerSavingInit(items);
        }
        if (Misc.hasPowerSavingWq()) {
            powerSavingWqInit(items);
        }
        if (Misc.hasCFSScheduler()) {
            cfsSchedulerInit(items);
        }
        if (Misc.hasCpuQuiet()) {
            cpuQuietInit(items);
        }
        if (CPUBoost.supported()) {
            cpuBoostInit(items);
        }
        if (Misc.hasCpuTouchBoost()) {
            cpuTouchBoostInit(items);
        }
        return items;
    }

    private void freqInit(List<RecyclerViewItem> items) {
        CardView bigCard = new CardView(getContext());
        if (CPUFreq.isBigLITTLE()) {
            bigCard.setTitle(getString(R.string.cluster_big));
        }

        mCPUMaxBig = new SelectView();
        mCPUMaxBig.setTitle(getString(R.string.cpu_max_freq));
        mCPUMaxBig.setSummary(getString(R.string.cpu_max_freq_summary));
        mCPUMaxBig.setItems(CPUFreq.getAdjustedFreq(getActivity()));
        mCPUMaxBig.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                List<Integer> cores = CPUFreq.getBigCpuRange();
                CPUFreq.setMaxFreq(CPUFreq.getFreqs().get(position), cores.get(0), cores.get(cores.size() - 1),
                        getActivity());
            }
        });
        bigCard.addItem(mCPUMaxBig);

        mCPUMinBig = new SelectView();
        mCPUMinBig.setTitle(getString(R.string.cpu_min_freq));
        mCPUMinBig.setSummary(getString(R.string.cpu_min_freq_summary));
        mCPUMinBig.setItems(CPUFreq.getAdjustedFreq(getActivity()));
        mCPUMinBig.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                List<Integer> cores = CPUFreq.getBigCpuRange();
                CPUFreq.setMinFreq(CPUFreq.getFreqs().get(position), cores.get(0), cores.get(cores.size() - 1),
                        getActivity());
            }
        });
        bigCard.addItem(mCPUMinBig);

        if (CPUFreq.hasMaxScreenOffFreq()) {
            mCPUMaxScreenOffBig = new SelectView();
            mCPUMaxScreenOffBig.setTitle(getString(R.string.cpu_max_screen_off_freq));
            mCPUMaxScreenOffBig.setSummary(getString(R.string.cpu_max_screen_off_freq_summary));
            mCPUMaxScreenOffBig.setItems(CPUFreq.getAdjustedFreq(getActivity()));
            mCPUMaxScreenOffBig.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    List<Integer> cores = CPUFreq.getBigCpuRange();
                    CPUFreq.setMaxScreenOffFreq(CPUFreq.getFreqs().get(position), cores.get(0), cores.get(cores.size() - 1),
                            getActivity());
                }
            });
            bigCard.addItem(mCPUMaxScreenOffBig);
        }

        mCPUGovernorBig = new SelectView();
        mCPUGovernorBig.setTitle(getString(R.string.cpu_governor));
        mCPUGovernorBig.setSummary(getString(R.string.cpu_governor_summary));
        mCPUGovernorBig.setItems(CPUFreq.getGovernors());
        mCPUGovernorBig.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                List<Integer> cores = CPUFreq.getBigCpuRange();
                CPUFreq.setGovernor(item, cores.get(0), cores.get(cores.size() - 1), getActivity());
            }
        });
        bigCard.addItem(mCPUGovernorBig);

        DescriptionView governorTunablesBig = new DescriptionView();
        governorTunablesBig.setTitle(getString(R.string.cpu_governor_tunables));
        governorTunablesBig.setSummary(getString(R.string.cpu_governor_tunables_summary));
        governorTunablesBig.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                showGovernorTunables(CPUFreq.getBigCpu());
            }
        });
        bigCard.addItem(governorTunablesBig);

        items.add(bigCard);

        if (CPUFreq.isBigLITTLE()) {
            CardView LITTLECard = new CardView(getContext());
            LITTLECard.setTitle(getString(R.string.cluster_little));

            mCPUMaxLITTLE = new SelectView();
            mCPUMaxLITTLE.setTitle(getString(R.string.cpu_max_freq));
            mCPUMaxLITTLE.setSummary(getString(R.string.cpu_max_freq_summary));
            mCPUMaxLITTLE.setItems(CPUFreq.getAdjustedFreq(CPUFreq.getLITTLECpu(), getActivity()));
            mCPUMaxLITTLE.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    List<Integer> cores = CPUFreq.getLITTLECpuRange();
                    CPUFreq.setMaxFreq(CPUFreq.getFreqs(CPUFreq.getLITTLECpu()).get(position),
                            cores.get(0), cores.get(cores.size() - 1), getActivity());
                }
            });
            LITTLECard.addItem(mCPUMaxLITTLE);

            mCPUMinLITTLE = new SelectView();
            mCPUMinLITTLE.setTitle(getString(R.string.cpu_min_freq));
            mCPUMinLITTLE.setSummary(getString(R.string.cpu_min_freq_summary));
            mCPUMinLITTLE.setItems(CPUFreq.getAdjustedFreq(CPUFreq.getLITTLECpu(), getActivity()));
            mCPUMinLITTLE.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    List<Integer> cores = CPUFreq.getLITTLECpuRange();
                    CPUFreq.setMinFreq(CPUFreq.getFreqs(CPUFreq.getLITTLECpu()).get(position),
                            cores.get(0), cores.get(cores.size() - 1), getActivity());
                }
            });
            LITTLECard.addItem(mCPUMinLITTLE);

            if (CPUFreq.hasMaxScreenOffFreq(CPUFreq.getLITTLECpu())) {
                mCPUMaxScreenOffLITTLE = new SelectView();
                mCPUMaxScreenOffLITTLE.setTitle(getString(R.string.cpu_max_screen_off_freq));
                mCPUMaxScreenOffLITTLE.setSummary(getString(R.string.cpu_max_screen_off_freq_summary));
                mCPUMaxScreenOffLITTLE.setItems(CPUFreq.getAdjustedFreq(CPUFreq.getLITTLECpu(), getActivity()));
                mCPUMaxScreenOffLITTLE.setOnItemSelected(new SelectView.OnItemSelected() {
                    @Override
                    public void onItemSelected(SelectView selectView, int position, String item) {
                        List<Integer> cores = CPUFreq.getLITTLECpuRange();
                        CPUFreq.setMaxScreenOffFreq(CPUFreq.getFreqs(CPUFreq.getLITTLECpu()).get(position),
                                cores.get(0), cores.get(cores.size() - 1), getActivity());
                    }
                });
                LITTLECard.addItem(mCPUMaxScreenOffLITTLE);
            }

            mCPUGovernorLITTLE = new SelectView();
            mCPUGovernorLITTLE.setTitle(getString(R.string.cpu_governor));
            mCPUGovernorLITTLE.setSummary(getString(R.string.cpu_governor_summary));
            mCPUGovernorLITTLE.setItems(CPUFreq.getGovernors());
            mCPUGovernorLITTLE.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    List<Integer> cores = CPUFreq.getLITTLECpuRange();
                    CPUFreq.setGovernor(item, cores.get(0), cores.get(cores.size() - 1), getActivity());
                }
            });
            LITTLECard.addItem(mCPUGovernorLITTLE);

            DescriptionView governorTunablesLITTLE = new DescriptionView();
            governorTunablesLITTLE.setTitle(getString(R.string.cpu_governor_tunables));
            governorTunablesLITTLE.setSummary(getString(R.string.cpu_governor_tunables_summary));
            governorTunablesLITTLE.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    showGovernorTunables(CPUFreq.getLITTLECpu());
                }
            });
            LITTLECard.addItem(governorTunablesLITTLE);

            items.add(LITTLECard);
        }
    }

    private void showGovernorTunables(int cpu) {
        String governor = CPUFreq.getGovernor(cpu, false);
        if (governor.isEmpty()) {
            mGovernorTunableErrorDialog = ViewUtils.dialogBuilder(getString(R.string.cpu_governor_tunables_read_error),
                    null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mGovernorTunableErrorDialog = null;
                        }
                    }, getActivity());
            mGovernorTunableErrorDialog.show();
        } else {
            setForegroundText(governor);
            mGovernorTunableFragment.setError(getString(R.string.cpu_governor_tunables_error, governor));
            mGovernorTunableFragment.setPath(CPUFreq.getGovernorTunablesPath(cpu, governor), cpu);
            showForeground();
        }
    }

    private void mcPowerSavingInit(List<RecyclerViewItem> items) {
        SelectView mcPowerSaving = new SelectView();
        mcPowerSaving.setTitle(getString(R.string.mc_power_saving));
        mcPowerSaving.setSummary(getString(R.string.mc_power_saving_summary));
        mcPowerSaving.setItems(Arrays.asList(getResources().getStringArray(R.array.mc_power_saving_items)));
        mcPowerSaving.setItem(Misc.getCurMcPowerSaving());
        mcPowerSaving.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                Misc.setMcPowerSaving(position, getActivity());
            }
        });

        items.add(mcPowerSaving);
    }

    private void powerSavingWqInit(List<RecyclerViewItem> items) {
        SwitchView powerSavingWq = new SwitchView();
        powerSavingWq.setSummary(getString(R.string.power_saving_wq));
        powerSavingWq.setChecked(Misc.isPowerSavingWqEnabled());
        powerSavingWq.setOnSwitchListener(new SwitchView.OnSwitchListener() {
            @Override
            public void onChanged(SwitchView switchView, boolean isChecked) {
                Misc.enablePowerSavingWq(isChecked, getActivity());
            }
        });

        items.add(powerSavingWq);
    }

    private void cfsSchedulerInit(List<RecyclerViewItem> items) {
        SelectView cfsScheduler = new SelectView();
        cfsScheduler.setTitle(getString(R.string.cfs_scheduler_policy));
        cfsScheduler.setSummary(getString(R.string.cfs_scheduler_policy_summary));
        cfsScheduler.setItems(Misc.getAvailableCFSSchedulers());
        cfsScheduler.setItem(Misc.getCurrentCFSScheduler());
        cfsScheduler.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                Misc.setCFSScheduler(item, getActivity());
            }
        });

        items.add(cfsScheduler);
    }

    private void cpuQuietInit(List<RecyclerViewItem> items) {
        if (Misc.hasCpuQuietEnable()) {
            SwitchView cpuQuietEnable = new SwitchView();
            cpuQuietEnable.setTitle(getString(R.string.cpu_quiet));
            cpuQuietEnable.setSummary(getString(R.string.cpu_quiet_summary));
            cpuQuietEnable.setChecked(Misc.isCpuQuietEnabled());
            cpuQuietEnable.setOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    Misc.enableCpuQuiet(isChecked, getActivity());
                }
            });

            items.add(cpuQuietEnable);
        }

        if (Misc.hasCpuQuietGovernors()) {
            SelectView cpuQuietGovernors = new SelectView();
            cpuQuietGovernors.setSummary(getString(R.string.cpu_quiet_governor));
            cpuQuietGovernors.setItems(Misc.getCpuQuietAvailableGovernors());
            cpuQuietGovernors.setItem(Misc.getCpuQuietCurGovernor());
            cpuQuietGovernors.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    Misc.setCpuQuietGovernor(item, getActivity());
                }
            });

            items.add(cpuQuietGovernors);
        }
    }

    private void cpuBoostInit(List<RecyclerViewItem> items) {
        List<RecyclerViewItem> cpuBoost = new ArrayList<>();

        TitleView title = new TitleView();
        title.setText(getString(R.string.cpu_boost));

        if (CPUBoost.hasEnable()) {
            SwitchView enable = new SwitchView();
            enable.setSummary(getString(R.string.cpu_boost));
            enable.setChecked(CPUBoost.isEnabled());
            enable.setOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    CPUBoost.enableCpuBoost(isChecked, getActivity());
                }
            });

            items.add(enable);
        }

        if (CPUBoost.hasCpuBoostDebugMask()) {
            SwitchView debugMask = new SwitchView();
            debugMask.setTitle(getString(R.string.debug_mask));
            debugMask.setSummary(getString(R.string.debug_mask_summary));
            debugMask.setChecked(CPUBoost.isCpuBoostDebugMaskEnabled());
            debugMask.setOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    CPUBoost.enableCpuBoostDebugMask(isChecked, getActivity());
                }
            });

            cpuBoost.add(debugMask);
        }

        if (CPUBoost.hasCpuBoostMs()) {
            SeekBarView ms = new SeekBarView();
            ms.setTitle(getString(R.string.interval));
            ms.setSummary(getString(R.string.interval_summary));
            ms.setUnit(getString(R.string.ms));
            ms.setMax(5000);
            ms.setOffset(10);
            ms.setProgress(CPUBoost.getCpuBootMs() / 10);
            ms.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    CPUBoost.setCpuBoostMs(position * 10, getActivity());
                }
            });

            cpuBoost.add(ms);
        }

        if (CPUBoost.hasCpuBoostSyncThreshold() && CPUFreq.getFreqs() != null) {
            List<String> list = new ArrayList<>();
            list.add(getString(R.string.disabled));
            list.addAll(CPUFreq.getAdjustedFreq(getActivity()));

            SelectView syncThreshold = new SelectView();
            syncThreshold.setTitle(getString(R.string.sync_threshold));
            syncThreshold.setSummary(getString(R.string.sync_threshold_summary));
            syncThreshold.setItems(list);
            syncThreshold.setItem(CPUBoost.getCpuBootSyncThreshold());
            syncThreshold.setOnItemSelected(new SelectView.OnItemSelected() {
                @Override
                public void onItemSelected(SelectView selectView, int position, String item) {
                    CPUBoost.setCpuBoostSyncThreshold(position == 0 ? 0 : CPUFreq.getFreqs().get(position - 1),
                            getActivity());
                }
            });

            cpuBoost.add(syncThreshold);
        }

        if (CPUBoost.hasCpuBoostInputMs()) {
            SeekBarView inputMs = new SeekBarView();
            inputMs.setTitle(getString(R.string.input_interval));
            inputMs.setSummary(getString(R.string.input_interval_summary));
            inputMs.setUnit(getString(R.string.ms));
            inputMs.setMax(5000);
            inputMs.setOffset(10);
            inputMs.setProgress(CPUBoost.getCpuBootInputMs() / 10);
            inputMs.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    CPUBoost.setCpuBoostInputMs(position * 10, getActivity());
                }
            });

            cpuBoost.add(inputMs);
        }

        if (CPUBoost.hasCpuBoostInputFreq() && CPUFreq.getFreqs() != null) {
            List<String> list = new ArrayList<>();
            list.add(getString(R.string.disabled));
            list.addAll(CPUFreq.getAdjustedFreq(getActivity()));

            List<Integer> freqs = CPUBoost.getCpuBootInputFreq();
            for (int i = 0; i < freqs.size(); i++) {
                SelectView inputCard = new SelectView();
                if (freqs.size() > 1) {
                    inputCard.setTitle(getString(R.string.input_boost_freq_core, i + 1));
                } else {
                    inputCard.setTitle(getString(R.string.input_boost_freq));
                }
                inputCard.setSummary(getString(R.string.input_boost_freq_summary));
                inputCard.setItems(list);
                inputCard.setItem(freqs.get(i));

                final int core = i;
                inputCard.setOnItemSelected(new SelectView.OnItemSelected() {
                    @Override
                    public void onItemSelected(SelectView selectView, int position, String item) {
                        CPUBoost.setCpuBoostInputFreq(position == 0 ? 0 : CPUFreq.getFreqs().get(position - 1),
                                core, getActivity());
                    }
                });
                cpuBoost.add(inputCard);
            }
        }

        if (CPUBoost.hasCpuBoostWakeup()) {
            SwitchView wakeup = new SwitchView();
            wakeup.setTitle(getString(R.string.wakeup_boost));
            wakeup.setSummary(getString(R.string.wakeup_boost_summary));
            wakeup.setChecked(CPUBoost.isCpuBoostWakeupEnabled());
            wakeup.setOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    CPUBoost.enableCpuBoostWakeup(isChecked, getActivity());
                }
            });

            cpuBoost.add(wakeup);
        }

        if (CPUBoost.hasCpuBoostHotplug()) {
            SwitchView hotplug = new SwitchView();
            hotplug.setTitle(getString(R.string.hotplug_boost));
            hotplug.setSummary(getString(R.string.hotplug_boost_summary));
            hotplug.setChecked(CPUBoost.isCpuBoostHotplugEnabled());
            hotplug.setOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    CPUBoost.enableCpuBoostHotplug(isChecked, getActivity());
                }
            });

            cpuBoost.add(hotplug);
        }

        if (cpuBoost.size() > 0) {
            items.add(title);
            items.addAll(cpuBoost);
        }
    }

    private void cpuTouchBoostInit(List<RecyclerViewItem> items) {
        SwitchView touchBoost = new SwitchView();
        touchBoost.setTitle(getString(R.string.touch_boost));
        touchBoost.setSummary(getString(R.string.touch_boost_summary));
        touchBoost.setChecked(Misc.isCpuTouchBoostEnabled());
        touchBoost.setOnSwitchListener(new SwitchView.OnSwitchListener() {
            @Override
            public void onChanged(SwitchView switchView, boolean isChecked) {
                Misc.enableCpuTouchBoost(isChecked, getActivity());
            }
        });

        items.add(touchBoost);
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mRefreshThread == null) {
            mRefreshThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        if (mCPUMaxBig != null) {
                            mCPUMaxFreqBig = CPUFreq.getMaxFreq(mCPUMaxFreqBig == 0);
                        }
                        if (mCPUMinBig != null) {
                            mCPUMinFreqBig = CPUFreq.getMinFreq(mCPUMinFreqBig == 0);
                        }
                        if (mCPUMaxScreenOffBig != null) {
                            mCPUMaxScreenOffFreqBig = CPUFreq.getMaxScreenOffFreq(mCPUMaxScreenOffFreqBig == 0);
                        }
                        if (mCPUGovernorBig != null) {
                            mCPUGovernorStrBig = CPUFreq.getGovernor(mCPUGovernorStrBig == null);
                        }
                        if (mCPUMaxLITTLE != null) {
                            mCPUMaxFreqLITTLE = CPUFreq.getMaxFreq(CPUFreq.getLITTLECpu(), mCPUMaxFreqLITTLE == 0);
                        }
                        if (mCPUMinLITTLE != null) {
                            mCPUMinFreqLITTLE = CPUFreq.getMinFreq(CPUFreq.getLITTLECpu(), mCPUMinFreqLITTLE == 0);
                        }
                        if (mCPUMaxScreenOffLITTLE != null) {
                            mCPUMaxScreenOffFreqLITTLE = CPUFreq.getMaxScreenOffFreq(CPUFreq.getLITTLECpu(),
                                    mCPUMaxScreenOffFreqLITTLE == 0);
                        }
                        if (mCPUGovernorLITTLE != null) {
                            mCPUGovernorStrLITTLE = CPUFreq.getGovernor(CPUFreq.getLITTLECpu(),
                                    mCPUGovernorStrLITTLE == null);
                        }

                        mRefreshThread = null;
                    }
                }
            });
            mRefreshThread.start();
        }
        if (mCPUMaxBig != null && mCPUMaxFreqBig != 0) {
            mCPUMaxBig.setItem((mCPUMaxFreqBig / 1000) + getString(R.string.mhz));
        }
        if (mCPUMinBig != null && mCPUMinFreqBig != 0) {
            mCPUMinBig.setItem((mCPUMinFreqBig / 1000) + getString(R.string.mhz));
        }
        if (mCPUMaxScreenOffBig != null && mCPUMaxScreenOffFreqBig != 0) {
            mCPUMaxScreenOffBig.setItem((mCPUMaxScreenOffFreqBig / 1000) + getString(R.string.mhz));
        }
        if (mCPUGovernorBig != null && mCPUGovernorStrBig != null && !mCPUGovernorStrBig.isEmpty()) {
            mCPUGovernorBig.setItem(mCPUGovernorStrBig);
        }
        if (mCPUMaxLITTLE != null && mCPUMaxFreqLITTLE != 0) {
            mCPUMaxLITTLE.setItem((mCPUMaxFreqLITTLE / 1000) + getString(R.string.mhz));
        }
        if (mCPUMinLITTLE != null && mCPUMinFreqLITTLE != 0) {
            mCPUMinLITTLE.setItem((mCPUMinFreqLITTLE / 1000) + getString(R.string.mhz));
        }
        if (mCPUMaxScreenOffLITTLE != null && mCPUMaxScreenOffFreqLITTLE != 0) {
            mCPUMaxScreenOffLITTLE.setItem((mCPUMaxScreenOffFreqLITTLE / 1000) + getString(R.string.mhz));
        }
        if (mCPUGovernorLITTLE != null && mCPUGovernorStrLITTLE != null && !mCPUGovernorStrLITTLE.isEmpty()) {
            mCPUGovernorLITTLE.setItem(mCPUGovernorStrLITTLE);
        }
    }

}